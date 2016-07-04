package com.github.yoojia.web.logging

import com.github.yoojia.web.Request
import com.github.yoojia.web.interceptor.BeforeInterceptor
import com.github.yoojia.web.supports.ALL
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.12
 */
@BeforeInterceptor
class BeforeLoggingHandler {

    companion object {

        @JvmStatic fun prepareLogText(request: Request): String {
            val buff = StringBuilder()
            buff.append("Request-Start: ").append(Date(request.createTime)).append("\r\n")
            buff.append("Request-Uri: ").append(request.path).append("\r\n")
            buff.append("Request-Method: ").append(request.method).append("\r\n")

            // headers
            buff.append("Request-Headers:").append("\r\n")
            for((name, value) in request.headers()) {
                buff.append("    ").append(name).append(" = ").append(value).append("\r\n")
            }

            // Cookies
            buff.append("Request-Cookies:").append("\r\n")
            request.cookies().forEach { cookie ->
                buff.append("    ").append(cookie).append("\r\n")
            }

            // params
            buff.append("Request-Params:").append("\r\n")
            for((name, value) in request.params()) {
                buff.append("    ").append(name).append(" = ").append(value).append("\r\n")
            }
            return buff.toString()
        }
    }

    @ALL("/*")
    fun loggingBefore(request: Request) {
        request.putParam(LOGGING_KEY, prepareLogText(request))
    }

}