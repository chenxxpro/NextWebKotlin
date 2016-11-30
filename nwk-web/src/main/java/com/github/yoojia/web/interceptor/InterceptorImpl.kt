package com.github.yoojia.web.interceptor

import com.github.yoojia.web.*
import com.github.yoojia.web.lang.concat
import com.github.yoojia.web.supports.Comparator
import com.github.yoojia.web.supports.RequestHandler
import org.slf4j.LoggerFactory
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.10
 */
abstract class InterceptorImpl(tag: String,
                               annotation: Class<out Annotation>,
                               classes: List<Class<*>>) : ModuleImpl(tag, annotation, classes) {
    companion object {
        private val Logger = LoggerFactory.getLogger(InterceptorImpl::class.java)
    }

    private val definedIgnores = ArrayList<Comparator>()

    override fun prepare(inputs: List<Class<*>>): List<Class<*>> {
        try{
            return super.prepare(inputs)
        }finally{
            handlers.filter{ it.javaMethod.isAnnotationPresent(Ignore::class.java) }.forEach { handler->
                val annotation = handler.javaMethod.getAnnotation(Ignore::class.java)
                val uris = annotation.value
                if(uris.isEmpty()) {
                    throw IllegalArgumentException("@Ignore must gives which URI to be ignore")
                }
                uris.forEach { uri ->
                    if(uri.isNullOrEmpty()) {
                        throw IllegalArgumentException("URI must not be null or empty")
                    }
                    val comparator = Comparator.createDefine(handler.comparator.method, concat(handler.root, uri))
                    definedIgnores.add(comparator)
                    Logger.info("$tag-Ignore-Define: $comparator , based: ${handler.comparator}")
                }
            }
        }
    }

    @Throws(Exception::class)
    override fun process(request: Request, response: Response, chain: RequestChain, router: Router) {
        if(handlers.isEmpty()) {
            router.next(request, response, chain, router)
        }else{
            val handlers = findMatches(request.comparator)
            invokeHandlers(handlers, request, response, chain)
            if (! chain.isInterrupted) {
                router.next(request, response, chain, router)
            }/*else{ 用户主动中断模块链的传递 }*/
        }
    }

    override fun findMatches(requestComparator: Comparator): List<RequestHandler> {
        definedIgnores.forEach { define ->
            if(requestComparator.isMatchDefine(define)) {
                return emptyList()
            }
        }
        return super.findMatches(requestComparator)
    }
}