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
            val buff = StringBuilder("===> START").append("\r\n")
            buff.append("Request-At: ").append(FORMATTER.format(Date(request.createTime))).append("\r\n")
            buff.append("Request-Uri: ").append(request.path).append("\r\n")
            buff.append("Request-Method: ").append(request.method).append("\r\n")
            val addSub = fun(name: String, value: Any) {
                buff.append("    ").append(name).append(": ").append(value).append("\r\n")
            }
            // headers
            val headers = request.headers()
            if (headers.isNotEmpty()) {
                buff.append("Headers:").append("\r\n")
                for((name, value) in request.headers()) { addSub(name, value) }
            }
            // Cookies
            val cookies = request.cookies()
            if(cookies.isNotEmpty()) {
                buff.append("Cookies:").append("\r\n")
                request.cookies().forEach { cookie -> addSub(cookie.name, cookie.value) }
            }
            // params
            val params = request.params()
            if(params.isNotEmpty()) {
                buff.append("Query-Parameters:").append("\r\n")
                for((name, value) in request.params()) { addSub(name, value) }
            }
            return buff.toString()
        }
    }

    @ALL("/*")
    fun loggingBefore(request: Request) {
        request.putParam(LOGGING_KEY, prepareLogText(request))
    }

}