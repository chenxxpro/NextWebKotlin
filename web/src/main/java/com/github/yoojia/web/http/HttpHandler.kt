package com.github.yoojia.web.http

import com.github.yoojia.web.*
import com.github.yoojia.web.supports.InternalPriority

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class HttpHandler(classes: List<Class<*>>) : ModuleImpl("HttpController", Controller::class.java, classes) {

    override fun getRootUri(hostType: Class<*>): String {
        return hostType.getAnnotation(Controller::class.java).value
    }

    @Throws(Exception::class)
    override fun process(request: Request, response: Response, router: Router) {
        val handlers = findMatches(request.comparator)
        if(handlers.isEmpty()) {
            super.process(request, response, router)
        }else{
            // 在HTTP模块存在处理器的时候，将HTTP状态码修改为 202 Accepted
            response.setStatusCode(StatusCode.ACCEPTED)
            val forward = invokeHandlers(handlers, request, response)
            if (forward) {
                super.process(request, response, router)
            }/*else{ 用户主动中断模块传递链 }*/
        }
    }

    companion object {
        @JvmField val DEFAULT_PRIORITY = InternalPriority.HTTP
    }
}