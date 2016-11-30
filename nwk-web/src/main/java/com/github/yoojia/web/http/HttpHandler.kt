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
    override fun process(request: Request, response: Response, chain: RequestChain, router: Router) {
        val handlers = findMatches(request.comparator)
        if(handlers.isEmpty()) {
            router.next(request, response, chain, router)
        }else{
            // 在HTTP模块存在处理器的时候，将HTTP状态码修改为 202 Accepted
            response.status(StatusCode.ACCEPTED)
            invokeHandlers(handlers, request, response, chain)
            if (! chain.isInterrupted) {
                router.next(request, response, chain, router)
            }/*else{ 用户主动中断模块链的传递 }*/
        }
    }

    companion object {
        @JvmField val DEFAULT_PRIORITY = InternalPriority.HTTP
    }
}