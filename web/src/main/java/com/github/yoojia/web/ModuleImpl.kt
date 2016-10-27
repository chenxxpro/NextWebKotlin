package com.github.yoojia.web

import com.github.yoojia.web.supports.*
import com.github.yoojia.web.supports.Comparator
import org.slf4j.LoggerFactory
import java.util.*


/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
abstract class ModuleImpl(val tag: String,
                          val annotation: Class<out Annotation>,
                          inputs: List<Class<*>>) : Module {

    companion object {
        private val Logger = LoggerFactory.getLogger(ModuleImpl::class.java)
    }

    /// 被多线程访问但保证只在主线初始化时才有写操作，请求处理过程只读操作
    private val cached = ArrayList<RequestHandler>()
    private val moduleCachedObjects: ModuleCachedProvider
    private val classes: ArrayList<Class<*>>

    protected val handlers: List<RequestHandler> by lazy { cached }

    init{
        val found = inputs.filter { it.isAnnotationPresent(annotation) }
        moduleCachedObjects = ModuleCachedProvider(found.size)
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
                cached.add(handler)
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
        cached.clear()
    }

    fun processHandlers(handlers: List<RequestHandler>, request: Request, response: Response, router: Router) {
        handlers.sortedBy { it.priority }.forEach { handler ->
            request.removeDynamics()
            val dynamics = getDynamics(handler, request)
            if (dynamics.isNotEmpty()) {
                request.putDynamics(dynamics)
                response.putArgs(dynamics) // copy to response
            }
            Logger.trace("$tag-Processing-Handler: $handler")
            val moduleObject = moduleCachedObjects.getCachedOrNew(handler.invoker.hostType)
            val chain = RequestChain()
            // 插入一些特殊处理过程接口的调用
            if(moduleObject is ModuleRequestsListener) {
                moduleObject.beforeRequests(handler.javaMethod, request, response)
                try{
                    handler.invoker.invoke(request, response, chain, moduleObject)
                }finally{
                    moduleObject.afterRequests(handler.javaMethod, request, response)
                }
            }else{
                handler.invoker.invoke(request, response, chain, moduleObject)
            }
            if(chain.isInterrupted()) return@forEach
            if(chain.isStopDispatching()) return
        }
        router.next(request, response, router)
    }

    protected open fun findMatches(requestComparator: Comparator): List<RequestHandler> {
        return cached.filter { define -> requestComparator.isMatchDefine(define.comparator) }
    }

    protected abstract fun getRootUri(hostType: Class<*>): String

    private fun getDynamics(handler: RequestHandler, request: Request): Map<String, String> {
        val output = mutableMapOf<String, String>()
        for(i in handler.comparator.segments.indices) {
            val segment = handler.comparator.segments[i]
            if(segment.dynamic) {
                output.put(segment.segment, request.resources[i])
            }
        }
        return if(output.isEmpty()) emptyMap() else output
    }
}