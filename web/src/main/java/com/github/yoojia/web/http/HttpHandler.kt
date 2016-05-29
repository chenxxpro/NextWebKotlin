package com.github.yoojia.web.http

import com.github.yoojia.web.Request
import com.github.yoojia.web.Response
import com.github.yoojia.web.StatusCode
import com.github.yoojia.web.kernel.DispatchChain
import com.github.yoojia.web.supports.AbstractHandler
import com.github.yoojia.web.supports.InternalPriority
import com.github.yoojia.web.util.RequestDefine

/**
 * HTTP 模块，处理HTTP请求
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class HttpHandler(classes: List<Class<*>>) :
        AbstractHandler("Http", Module::class.java, classes) {

    override fun getBaseUri(hostType: Class<*>): String {
        return hostType.getAnnotation(Module::class.java).base
    }

    @Throws(Exception::class)
    override fun process(request: Request, response: Response, dispatch: DispatchChain) {
        val matched = findMatched(RequestDefine(listOf(request.method), request.resources));
        // 在HTTP模块存在处理器的时候，将HTTP状态码修改为 202 Accepted
        if(matched.isNotEmpty()) {
            response.setStatusCode(StatusCode.ACCEPTED)
        }
        processMatches(matched, request, response, dispatch)
    }

    companion object {
        val DEFAULT_PRIORITY = InternalPriority.HTTP
    }
}