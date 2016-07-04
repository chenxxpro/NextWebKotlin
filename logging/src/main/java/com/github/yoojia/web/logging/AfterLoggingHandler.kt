package com.github.yoojia.web.logging

import com.github.yoojia.web.Request
import com.github.yoojia.web.Response
import com.github.yoojia.web.interceptor.BeforeInterceptor
import com.github.yoojia.web.supports.ALL
import com.github.yoojia.web.util.param
import org.slf4j.LoggerFactory
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.12
 */
@BeforeInterceptor
class AfterLoggingHandler {

    companion object {
        private val Logger = LoggerFactory.getLogger(AfterLoggingHandler::class.java)

        fun prepareLog(response: Response): String {
            val buff = StringBuilder()
            buff.append("Response-At: ").append(FORMATTER.format(Date(response.createTime))).append("\r\n")
            buff.append("Status-Code: ").append(response.servletResponse.status).append("\r\n")
            buff.append("Content-Type: ").append(response.servletResponse.contentType).append("\r\n")
            return buff.toString()
        }
    }

    @ALL("/*")
    fun loggingAfter(request: Request, response: Response) {
        val pre = request.param(LOGGING_KEY, "")
        if(!pre.isNullOrEmpty()) {
            val buff = StringBuilder(pre)
            buff.append(prepareLog(response))
            buff.append("<=== END")
            Logger.debug(buff.toString())
        }
    }

}