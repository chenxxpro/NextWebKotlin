package com.github.yoojia.web.http

import com.github.yoojia.web.*
import com.github.yoojia.web.supports.InternalPriority

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class HttpControllerHandler(classes: List<Class<*>>) : ModuleHandler("HttpController", Controller::class.java, classes) {

    override fun getRootUri(hostType: Class<*>): String {
        return hostType.getAnnotation(Controller::class.java).value
    }

    @Throws(Exception::class)
    override fun process(request: Request, response: Response, router: Router) {
        val matches = findMatches(request.comparator)
        // 在HTTP模块存在处理器的时候，将HTTP状态码修改为 202 Accepted
        if(matches.isNotEmpty()) {
            response.setStatusCode(StatusCode.ACCEPTED)
            processHandlers(matches, request, response, router)
        }
        super.process(request, response, router)
    }

    companion object {
        @JvmField val DEFAULT_PRIORITY = InternalPriority.HTTP
    }
}