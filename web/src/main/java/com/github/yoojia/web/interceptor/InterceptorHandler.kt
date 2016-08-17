package com.github.yoojia.web.interceptor

import com.github.yoojia.web.AbstractModuleHandler
import com.github.yoojia.web.Request
import com.github.yoojia.web.Response
import com.github.yoojia.web.core.DispatchChain
import com.github.yoojia.web.supports.Comparator
import com.github.yoojia.web.supports.RequestHandler
import com.github.yoojia.web.util.concat
import org.slf4j.LoggerFactory
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.10
 */
abstract class InterceptorHandler(tag: String,
                                   annotation: Class<out Annotation>,
                                   classes: List<Class<*>>) : AbstractModuleHandler(tag, annotation, classes) {
    companion object {
        private val Logger = LoggerFactory.getLogger(InterceptorHandler::class.java)
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
    override fun process(request: Request, response: Response, dispatch: DispatchChain) {
        if(handlers.isNotEmpty()) {
            val found = findMatches(request.comparator)
            processFound(found, request, response, dispatch)
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