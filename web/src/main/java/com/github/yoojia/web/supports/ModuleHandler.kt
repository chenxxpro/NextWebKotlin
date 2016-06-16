package com.github.yoojia.web.supports

import com.github.yoojia.web.Request
import com.github.yoojia.web.RequestChain
import com.github.yoojia.web.Response
import com.github.yoojia.web.core.Config
import com.github.yoojia.web.core.Context
import com.github.yoojia.web.core.DispatchChain
import com.github.yoojia.web.core.Module
import org.slf4j.LoggerFactory
import java.util.*


/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
abstract class ModuleHandler(val tag: String,
                             val annotation: Class<out Annotation>,
                             classes: List<Class<*>>) : Module {

    private val handlers = ArrayList<RequestHandler>()
    private val moduleObjectProvider: ModuleCachedProvider

    private val cachedClasses: ArrayList<Class<*>>
    
    companion object {
        private val Logger = LoggerFactory.getLogger(ModuleHandler::class.java)
    }

    init{
        val accepted = classes.filter {
            it.isAnnotationPresent(annotation)
        }
        moduleObjectProvider = ModuleCachedProvider(accepted.size)
        cachedClasses = ArrayList(accepted)
    }

    override fun onCreated(context: Context, config: Config) {
        // NOP
    }

    override fun onDestroy() {
        handlers.clear()
    }

    override fun prepare(classes: List<Class<*>>): List<Class<*>> {
        // 查找各个模块类的methods, 将模块类中用户定义的了注解@GET/POST/PUT/DELETE等方法，生成对应的RequestHandler
        cachedClasses.forEach { objectType ->
            val moduleUri = getModuleConfigUri(objectType)
            annotatedMethods(objectType, action = { javaMethod, annotationType ->
                checkReturnType(javaMethod)
                checkArguments(javaMethod)
                val handler = RequestHandler.create(moduleUri, objectType, javaMethod, annotationType)
                handlers.add(handler)
                Logger.info("$tag-Module-Define: $handler")
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
        val found = findMatched(RequestWrapper.request(request.method, request.path, request.resources))
        processFound(found, request, response, dispatch)
    }

    fun processFound(found: List<RequestHandler>, request: Request, response: Response, dispatch: DispatchChain) {
        Logger.trace("$tag-Module-Processing: ${request.path}")
        // 根据优先级排序后处理
        val sorted = found.sortedBy { it.priority }
        sorted.forEach { handler ->
            /*
                每个模块处理器在执行之前，清除前面处理器的动态参数：
                - 像 /users/{username} 中定义的动态参数 username 只对 @GET("/users/{username}") 所声明的方法函数有效，
                - 而对其它同样匹配路径如 @GET("/users/ *") 来说，动态参数中如果突然出现 username 参数值将会显得非常怪异。
            */
            request._resetDynamicScope()
            //  每个@GET/POST/PUT/DELETE方法Handler定义了不同的处理URI地址, 这里需要解析动态URL，并保存到Request中
            val dynamic = handler.request.parseDynamic(request.resources)
            if(dynamic.isNotEmpty()) {
                request._setDynamicScope(dynamic)
            }
            Logger.trace("$tag-Working-Processor: $handler")
            val moduleObject = moduleObjectProvider.get(handler.invoker.hostType)
            val chain = RequestChain()
            // 插入一些特殊处理过程接口的调用
            if(moduleObject is ModuleRequestsListener) {
                moduleObject.eachBefore(handler.javaMethod, request, response)
                try{
                    handler.invoker.invoke(request, response, chain, moduleObject)
                }finally{
                    moduleObject.eachAfter(handler.javaMethod, request, response)
                }
            }else{
                handler.invoker.invoke(request, response, chain, moduleObject)
            }
            // 处理器要求中断
            if(chain.isInterrupted()) return@forEach
            if(chain.isStopDispatching()) return
        }
        // 继续下一级模块的处理
        dispatch.next(request, response, dispatch)
    }

    protected fun findMatched(request: RequestWrapper): List<RequestHandler> {
        val found = ArrayList<RequestHandler>()
        handlers.forEach { define ->
            if(request.isRequestMatchDefine(define.request)) {
                found.add(define)
            }
        }
        return found
    }

    protected abstract fun getModuleConfigUri(hostType: Class<*>): String

}