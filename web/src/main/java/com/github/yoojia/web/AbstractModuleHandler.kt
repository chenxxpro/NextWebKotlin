package com.github.yoojia.web

import com.github.yoojia.web.core.Config
import com.github.yoojia.web.core.Context
import com.github.yoojia.web.core.DispatchChain
import com.github.yoojia.web.core.Module
import com.github.yoojia.web.supports.*
import com.github.yoojia.web.supports.Comparator
import org.slf4j.LoggerFactory
import java.util.*


/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
abstract class AbstractModuleHandler(val tag: String,
                                     val annotation: Class<out Annotation>,
                                     inputs: List<Class<*>>) : Module {

    companion object {
        private val Logger = LoggerFactory.getLogger(AbstractModuleHandler::class.java)
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

    override fun prepare(/*Ignore*/inputs: List<Class<*>>): List<Class<*>> {
        classes.forEach { clazz ->
            val root = getRootUri(clazz)
            findAnnotated(clazz, action = { javaMethod, httpMethod, path ->
                checkReturnType(javaMethod)
                checkArguments(javaMethod)
                val handler = RequestHandler.create(root, clazz, javaMethod, httpMethod, path)
                handlers.add(handler)
                Logger.info("$tag-Module-Define: $handler")
            })
        }
        try{
            return classes.toList()
        }finally{
            classes.clear()
        }
    }

    override fun onDestroy() {
        handlers.clear()
    }

    @Throws(Exception::class)
    override fun process(request: Request, response: Response, dispatch: DispatchChain) {
        val found = findMatches(request.comparator)
        processFound(found, request, response, dispatch)
    }

    fun processFound(found: List<RequestHandler>, request: Request, response: Response, dispatch: DispatchChain) {
        found.sortedBy { it.priority }.forEach { handler ->
            request.removeDynamicScopeParams()
            val dynamics = getDynamicParams(handler, request)
            if (dynamics.isNotEmpty()) {
                request.putDynamicScopeParams(dynamics)
                response.putArgs(dynamics) // copy to response
            }
            Logger.trace("$tag-Processing-Handler: $handler")
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

    protected open fun findMatches(requestComparator: Comparator): List<RequestHandler> {
        return handlers.filter { define -> requestComparator.isMatchDefine(define.comparator) }
    }

    protected abstract fun getRootUri(hostType: Class<*>): String

    private fun getDynamicParams(handler: RequestHandler, request: Request): Map<String, String> {
        val output = mutableMapOf<String, String>()
        for(i in handler.comparator.segments.indices) {
            val segment = handler.comparator.segments[i]
            if(segment.isDynamic) {
                output.put(segment.segment, request.resources[i])
            }
        }
        return if(output.isEmpty()) emptyMap() else output
    }
}