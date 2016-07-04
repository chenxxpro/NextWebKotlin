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
            buff.append("Response-Code:").append(response.servletResponse.status)
            buff.append("Content-Type").append(" = ").append(response.servletResponse.contentType).append("\r\n")
            buff.append("Response-End: ").append(Date(response.createTime)).append("\r\n")
            return buff.toString()
        }
    }

    @ALL("/*")
    fun loggingAfter(request: Request, response: Response) {
        val buff = StringBuilder(request.param(LOGGING_KEY))
        buff.append(prepareLog(response))
        Logger.debug(buff.toString())
    }

}