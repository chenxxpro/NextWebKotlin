package com.github.yoojia.web

import com.github.yoojia.web.supports.Comparator
import com.github.yoojia.web.supports.InternalPriority
import org.slf4j.LoggerFactory
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.13
 */
class HttpBeforeLogger : HttpLogger() {

    companion object {

        private val Logger = LoggerFactory.getLogger(HttpBeforeLogger::class.java)

        private fun prepareRequestLog(request: Request): String {
            val buff = StringBuilder("\r\n <=== START ===>").append("\r\n")
            addLine("Request-At", FORMATTER.format(Date(request.createTime)), buff)
            addLine("Request-Uri", request.path, buff)
            addLine("Request-Method", request.method, buff)
            // headers
            val headers = request.headers()
            if (headers.isNotEmpty()) {
                buff.append("Headers:").append("\r\n")
                for((name, value) in request.headers()) { addSubLine(name, value, buff) }
            }
            // Cookies
            val cookies = request.cookies()
            if(cookies.isNotEmpty()) {
                buff.append("Cookies:").append("\r\n")
                request.cookies().forEach { cookie -> addSubLine(cookie.name, cookie.value, buff) }
            }
            // params
            val params = request.params()
            if(params.isNotEmpty()) {
                buff.append("Query-Parameters:").append("\r\n")
                for((name, value) in request.params()) { addSubLine(name, value, buff) }
            }
            return buff.toString()
        }

        @JvmStatic val DEFAULT_PRIORITY = InternalPriority.LOGGING_BEFORE

    }

    private val ignores = ArrayList<Comparator>()

    override fun onCreated(context: Context, config: Config) {
        config.getTypedList<String>("uri-ignores").forEach { uri ->
            val path = if(uri.endsWith("/")) "$uri/*" else uri
            Logger.debug("BeforeLogger-URI-Ignore: $path")
            ignores.add(Comparator.createDefine("ALL", path))
        }
    }

    override fun onDestroy() {

    }

    override fun process(request: Request, response: Response, chain: RequestChain, router: Router) {
        var enabled = true
        ignores.forEach { define ->
            if(request.comparator.isMatchDefine(define)) {
                enabled = false
                return@forEach
            }
        }
        if(enabled) {
            request.putParam(LOGGING_TEXT_NAME, prepareRequestLog(request))
            request.putParam(LOGGING_ENABLED_NAME, true)
        }
        router.next(request, response, chain, router)
    }
}