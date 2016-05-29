package com.github.yoojia.web.supports

import com.github.yoojia.web.Request
import com.github.yoojia.web.RequestChain
import com.github.yoojia.web.Response
import com.github.yoojia.web.kernel.Config
import com.github.yoojia.web.kernel.Context
import com.github.yoojia.web.kernel.DispatchChain
import com.github.yoojia.web.kernel.Module
import com.github.yoojia.web.util.*
import java.util.*

/**
 * HTTP 模块，处理HTTP请求
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
abstract class AbstractHandler(val handlerTag: String,
                               val annotation: Class<out Annotation>,
                               classes: List<Class<*>>) : Module {

    private val mProcessors = ArrayList<MethodDefine>()
    private val mHostedObjectProvider: ObjectProvider

    private val mCachedClasses: ArrayList<Class<*>>

    init{
        val accepted = classes.filter {
            it.isAnnotationPresent(annotation)
        }
        mHostedObjectProvider = ObjectProvider(accepted.size)
        mCachedClasses = ArrayList(accepted)
    }

    override fun onCreated(context: Context, config: Config) {
        // NOP
    }

    override fun onDestroy() {
        mProcessors.clear()
    }

    override fun prepare(classes: List<Class<*>>): List<Class<*>> {
        // 解析各个Class的方法,并创建对应的MethodProcessor
        mCachedClasses.forEach { hostType ->
            val basedUri = getBaseUri(hostType)
            filterRouteAnnotated(hostType, { method ->
                checkReturnType(method)
                checkArguments(method)
                val define = createMethodDefine(basedUri, hostType, method)
                mProcessors.add(define)
                Logger.v("$handlerTag-Module-Define: $define")
            })
        }
        try{
            return mCachedClasses.toList()
        }finally{
            // prepare 完成之后可以清除缓存
            mCachedClasses.clear()
        }
    }

    @Throws(Exception::class)
    override fun process(request: Request, response: Response, dispatch: DispatchChain) {
        val matched = findMatched(RequestDefine(listOf(request.method), request.resources))
        processMatches(matched, request, response, dispatch)
    }

    fun processMatches(matched: List<MethodDefine>, request: Request, response: Response, dispatch: DispatchChain) {
        Logger.vv("$handlerTag-Module-Processing: ${request.path}")
        Logger.vv("$handlerTag-Module-Handlers: ${matched.size}")
        // 根据优先级排序后处理
        matched.sortedBy { it.priority }
                .forEach { method ->
                    /*
                        每个模块处理前清除前一模块的动态参数：
                        像 /users/{username} 中定义的动态参数 username 只对 @Route(path = "/users/{username}") 所声明的方法有效，
                        而对其它同样匹配路径如 @Route(path = "/users/ *") 来说，动态参数中突然出现 username 显得非常怪异。
                    */
                    request.clearDynamicParams()
                    //  每个@Route方法Handler定义了不同的处理URI地址, 这里需要解析动态URL，并保存到Request中
                    val params = dynamicParams(request.resources, method.request)
                    if(params.isNotEmpty()) {
                        request.putDynamicParams(params)
                    }
                    val chain = RequestChain()
                    Logger.vv("$handlerTag-Working-Processor: $method")
                    method.processor.invoke(request, response, chain, {
                        type -> mHostedObjectProvider.get(type)
                    })
                    // 处理器要求中断
                    //
                    if(chain.isInterrupted()) return@forEach
                    if(chain.isStopDispatching()) return
                }
        // 继续下一级模块的处理
        dispatch.next(request, response, dispatch)
    }

    protected fun findMatched(request: RequestDefine): List<MethodDefine> {
        val out = ArrayList<MethodDefine>()
        mProcessors.forEach { processor ->
            if(isRequestMatched(request, processor.request)) {
                out.add(processor)
            }
        }
        return out
    }

    protected abstract fun getBaseUri(hostType: Class<*>): String

}