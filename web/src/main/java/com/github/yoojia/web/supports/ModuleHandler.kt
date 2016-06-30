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
                             inputs: List<Class<*>>) : Module {

    companion object {
        private val Logger = LoggerFactory.getLogger(ModuleHandler::class.java)
    }

    protected  val handlers = ArrayList<RequestHandler>()

    private val objectProvider: ModuleCachedProvider
    private val classes: ArrayList<Class<*>>

    init{
        val found = inputs.filter { it.isAnnotationPresent(annotation) }
        objectProvider = ModuleCachedProvider(found.size)
        classes = ArrayList(found)
    }

    override fun onCreated(context: Context, config: Config) {
        // NOP
    }

    override fun prepare(inputs: List<Class<*>>): List<Class<*>> {
        inputs.forEach { clazz ->
            val root = getRootUri(clazz)
            findAnnotated(clazz, action = { javaMethod, annotationType ->
                checkReturnType(javaMethod)
                checkArguments(javaMethod)
                val handler = RequestHandler.create(root, clazz, javaMethod, annotationType)
                handlers.add(handler)
                Logger.info("$tag-Module-Define: $handler")
            })
        }
        try{
            return inputs.toList()
        }finally{
            classes.clear()
        }
    }

    override fun onDestroy() {
        handlers.clear()
    }

    @Throws(Exception::class)
    override fun process(request: Request, response: Response, dispatch: DispatchChain) {
        val found = findMatched(RequestWrapper.createFromClient(request.method, request.path, request.resources))
        processFound(found, request, response, dispatch)
    }

    fun processFound(found: List<RequestHandler>, request: Request, response: Response, dispatch: DispatchChain) {
        Logger.trace("$tag-Accepted: ${request.path}")
        found.sortedBy { it.priority }.forEach { handler ->
            request._resetDynamicScope()
            val dynamic = handler.request.parseDynamic(request.resources)
            if(dynamic.isNotEmpty()) {
                request._setDynamicScope(dynamic)
            }
            Logger.trace("$tag-Handler: $handler")
            val moduleObject = objectProvider.get(handler.invoker.hostType)
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

    protected open fun findMatched(request: RequestWrapper): List<RequestHandler> {
        val found = ArrayList<RequestHandler>()
        handlers.forEach { define ->
            if(request.isMatchDefine(define.request)) {
                found.add(define)
            }
        }
        return found
    }

    protected abstract fun getRootUri(hostType: Class<*>): String

}