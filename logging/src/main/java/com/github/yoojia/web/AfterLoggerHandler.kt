package com.github.yoojia.web

import com.github.yoojia.web.core.Config
import com.github.yoojia.web.core.Context
import com.github.yoojia.web.core.DispatchChain
import com.github.yoojia.web.supports.InternalPriority
import com.github.yoojia.web.util.param
import org.slf4j.LoggerFactory
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.13
 */
class AfterLoggerHandler : LoggerModule() {

    companion object {

        private fun prepareResponseLog(response: Response): String {
            val buff = StringBuilder()
            addLine("Response-At", FORMATTER.format(Date(response.createTime)), buff)
            addLine("Status-Code", response.servletResponse.status, buff)
            response.servletResponse.contentType?.let { contentType->
                addLine("Content-Type", contentType, buff)
            }
            return buff.toString()
        }

        private val Logger = LoggerFactory.getLogger(AfterLoggerHandler::class.java)

        @JvmStatic val DEFAULT_PRIORITY = InternalPriority.LOGGER_AFTER
    }

    override fun onCreated(context: Context, config: Config) {
        // nop
    }

    override fun onDestroy() {
        // nop
    }

    override fun process(request: Request, response: Response, dispatch: DispatchChain) {
        val prepared = request.param(LOGGING_ENABLED_NAME, false)
        val text = request.param(LOGGING_TEXT_NAME, "")
        if(prepared && text.isNotEmpty()) {
            val buff = StringBuilder(text)
            buff.append(prepareResponseLog(response))
            buff.append("<=== END ===>")
            Logger.debug(buff.toString())
        }
        super.process(request, response, dispatch)
    }
}