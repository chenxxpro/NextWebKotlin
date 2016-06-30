package com.github.yoojia.web.interceptor

import com.github.yoojia.web.supports.ModuleHandler
import com.github.yoojia.web.supports.RequestHandler
import com.github.yoojia.web.supports.RequestWrapper
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

    private val ignores = ArrayList<RequestWrapper>()

    override fun prepare(inputs: List<Class<*>>): List<Class<*>> {
        try{
            return super.prepare(inputs)
        }finally{
            handlers.forEach { handler->
                val method = handler.javaMethod
                if(method.isAnnotationPresent(Ignore::class.java)) {
                    val annotation = method.getAnnotation(Ignore::class.java)
                    if(annotation.value.isEmpty()) {
                        throw IllegalArgumentException("@Ignore must noted that which uri to ignore")
                    }
                    annotation.value.forEach { uri ->
                        val wrapper = RequestWrapper.createFromDefine(handler.request.method, concat(handler.root, uri))
                        ignores.add(wrapper)
                        Logger.info("$tag-Ignore-Define: $wrapper , based: ${handler.request}")
                    }
                }
            }
        }
    }

    override fun findMatched(request: RequestWrapper): List<RequestHandler> {
        ignores.forEach { define ->
            if(request.isMatchDefine(define)) {
                return emptyList()
            }
        }
        return super.findMatched(request)
    }
}