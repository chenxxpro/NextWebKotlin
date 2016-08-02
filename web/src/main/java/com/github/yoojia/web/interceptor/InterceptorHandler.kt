package com.github.yoojia.web.interceptor

import com.github.yoojia.web.supports.ModuleHandler
import com.github.yoojia.web.supports.RequestHandler
import com.github.yoojia.web.supports.Comparator
import com.github.yoojia.web.util.concat
import org.slf4j.LoggerFactory
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.10
 */
abstract class InterceptorHandler(tag: String,
                                   annotation: Class<out Annotation>,
                                   classes: List<Class<*>>) : ModuleHandler(tag, annotation, classes) {
    companion object {
        private val Logger = LoggerFactory.getLogger(InterceptorHandler::class.java)
    }

    private val ignores = ArrayList<Comparator>()

    override fun prepare(inputs: List<Class<*>>): List<Class<*>> {
        try{
            return super.prepare(inputs)
        }finally{
            handlers.forEach { handler->
                val method = handler.javaMethod
                if(method.isAnnotationPresent(Ignore::class.java)) {
                    val annotation = method.getAnnotation(Ignore::class.java)
                    if(annotation.value.isEmpty()) {
                        throw IllegalArgumentException("@Ignore must gives which URI to be ignore")
                    }
                    annotation.value.forEach { uri ->
                        if(uri.isNullOrEmpty()) throw IllegalArgumentException("URI must not be null or empty")
                        val comparator = Comparator.createDefine(handler.comparator.method, concat(handler.root, uri))
                        ignores.add(comparator)
                        Logger.info("$tag-Ignore-Define: $comparator , based: ${handler.comparator}")
                    }
                }
            }
        }
    }

    override fun findMatches(requestComparator: Comparator): List<RequestHandler> {
        ignores.forEach { define ->
            if(requestComparator.isMatchDefine(define)) {
                return emptyList()
            }
        }
        return super.findMatches(requestComparator)
    }
}