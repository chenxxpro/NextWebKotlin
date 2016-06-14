package com.github.yoojia.web.supports

import com.github.yoojia.web.Request
import com.github.yoojia.web.RequestChain
import com.github.yoojia.web.Response
import com.github.yoojia.web.core.Config
import com.github.yoojia.web.core.Context
import com.github.yoojia.web.core.DispatchChain
import com.github.yoojia.web.core.Module
import com.github.yoojia.web.util.*
import org.slf4j.LoggerFactory
import java.util.*


/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
abstract class ModuleHandler(val handlerTag: String,
                             val annotation: Class<out Annotation>,
                             classes: List<Class<*>>) : Module {

    private val processors = ArrayList<MethodMeta>()
    private val hostedObjectProvider: ModuleCachedProvider

    private val cachedClasses: ArrayList<Class<*>>
    
    companion object {
        private val Logger = LoggerFactory.getLogger(ModuleHandler::class.java)
    }

    init{
        val accepted = classes.filter {
            it.isAnnotationPresent(annotation)
        }
        hostedObjectProvider = ModuleCachedProvider(accepted.size)
        cachedClasses = ArrayList(accepted)
    }

    override fun onCreated(context: Context, config: Config) {
        // NOP
    }

    override fun onDestroy() {
        processors.clear()
    }

    override fun prepare(classes: List<Class<*>>): List<Class<*>> {
        // 解析各个Class的方法,并创建对应的MethodProcessor
        cachedClasses.forEach { hostType ->
            val basedUri = getBaseUri(hostType)
            filterAnnotatedMethods(hostType, { method, annotationType ->
                checkReturnType(method)
                checkArguments(method)
                val define = createMethodDefine(basedUri, hostType, method, annotationType)
                processors.add(define)
                Logger.info("$handlerTag-Module-Define: $define")
            })
        }
        try{
            return cachedClasses.toList()
        }finally{
            // prepare 完成之后可以清除缓存
            cachedClasses.clear()
        }
    }

    @Throws(Exception::class)
    override fun process(request: Request, response: Response, dispatch: DispatchChain) {
        val found = findMatched(RequestMeta.forClient(request.method, request.path, request.resources))
        processFound(found, request, response, dispatch)
    }

    fun processFound(found: List<MethodMeta>, request: Request, response: Response, dispatch: DispatchChain) {
        Logger.info("$handlerTag-Module-Processing: ${request.path}")
        Logger.info("$handlerTag-Module-Handlers: ${found.size}")
        // 根据优先级排序后处理
        found.sortedBy { it.priority }
                .forEach { handler ->
                    /*
                        每个模块处理器在执行之前，清除前面处理器的动态参数：
                        - 像 /users/{username} 中定义的动态参数 username 只对 @GET("/users/{username}") 所声明的方法函数有效，
                        - 而对其它同样匹配路径如 @GET("/users/ *") 来说，动态参数中如果突然出现 username 参数值将会显得非常怪异。
                    */
                    request._resetDynamicScope()
                    //  每个@GET/POST/PUT/DELETE方法Handler定义了不同的处理URI地址, 这里需要解析动态URL，并保存到Request中
                    val params = dynamicParams(request.resources, handler.request)
                    if(params.isNotEmpty()) {
                        request._setDynamicScope(params)
                    }
                    Logger.info("$handlerTag-Working-Processor: $handler")
                    val moduleObject = hostedObjectProvider.get(handler.processor.hostType)
                    val chain = RequestChain()
                    // 处理模块对象实现的特殊接口
                    if(moduleObject is ModuleRequestsListener) {
                        moduleObject.eachBefore(handler.javaMethod, request, response)
                        try{
                            handler.processor.invoke(request, response, chain, moduleObject)
                        }finally{
                            moduleObject.eachAfter(handler.javaMethod, request, response)
                        }
                    }else{
                        handler.processor.invoke(request, response, chain, moduleObject)
                    }
                    // 处理器要求中断
                    if(chain.isInterrupted()) return@forEach
                    if(chain.isStopDispatching()) return
                }
        // 继续下一级模块的处理
        dispatch.next(request, response, dispatch)
    }

    protected fun findMatched(request: RequestMeta): List<MethodMeta> {
        val out = ArrayList<MethodMeta>()
        processors.forEach { processor ->
            if(isRequestMatched(request, processor.request)) {
                out.add(processor)
            }
        }
        return out
    }

    protected abstract fun getBaseUri(hostType: Class<*>): String

}