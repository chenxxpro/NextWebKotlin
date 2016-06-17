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
        val sorted = found.sortedBy { it.priority }
        sorted.forEach { handler ->
            request._resetDynamicScope()
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
            if(chain.isInterrupted()) return@forEach
            if(chain.isStopDispatching()) return
        }
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