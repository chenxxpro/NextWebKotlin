package com.github.yoojia.web.http

import com.github.yoojia.web.Request
import com.github.yoojia.web.Response
import com.github.yoojia.web.StatusCode
import com.github.yoojia.web.core.DispatchChain
import com.github.yoojia.web.supports.InternalPriority
import com.github.yoojia.web.supports.ModuleHandler
import com.github.yoojia.web.supports.RequestWrapper

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class HttpControllerHandler(classes: List<Class<*>>) : ModuleHandler("HttpController", Controller::class.java, classes) {

    override fun getModuleConfigUri(hostType: Class<*>): String {
        return hostType.getAnnotation(Controller::class.java).value
    }

    @Throws(Exception::class)
    override fun process(request: Request, response: Response, dispatch: DispatchChain) {
        val found = findMatched(RequestWrapper.request(request.method, request.path, request.resources));
        // 在HTTP模块存在处理器的时候，将HTTP状态码修改为 202 Accepted
        if(found.isNotEmpty()) {
            response.setStatusCode(StatusCode.ACCEPTED)
        }
        processFound(found, request, response, dispatch)
    }

    companion object {
        val DEFAULT_PRIORITY = InternalPriority.HTTP
    }
}