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
    private val instances: ModuleCachedProvider
    private val classes: ArrayList<Class<*>>

    protected val handlers: List<RequestHandler> by lazy { cached }

    init{
        val found = inputs.filter { it.isAnnotationPresent(annotation) }
        instances = ModuleCachedProvider(found.size)
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
                if (Logger.isInfoEnabled) {
                    Logger.info("$tag-Define: $handler")
                }
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

    /**
     * 执行指定RequestHandler列表，返回是否继续运行模块链的标记。
     * @return True为继续传递，False则中断
     */
    protected fun invokeHandlers(handlers: List<RequestHandler>, request: Request, response: Response): Boolean {
        handlers.sortedBy { it.priority }.forEach { handler ->
            request.removeDynamics()
            val dynamics = dynamics(handler, request)
            if (dynamics.isNotEmpty()) {
                request.putDynamics(dynamics)
                response.params(dynamics) // copy to response
            }
            if (Logger.isTraceEnabled) {
                Logger.trace("$tag-Processing: $handler")
            }
            val moduleObject = instances.getOrNew(handler.invoker.classType)
            // 为每个请求处理器创建一个独立的 RequestChain 临时对象，生命周期只限于当前请求处理器。
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
            if (chain.isInterrupted) {
                return false
            }
        }
        return true
    }

    protected open fun findMatches(requestComparator: Comparator): List<RequestHandler> {
        return cached.filter { define -> requestComparator.isMatchDefine(define.comparator) }
    }

    protected abstract fun getRootUri(hostType: Class<*>): String

    private fun dynamics(handler: RequestHandler, request: Request): Map<String, String> {
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