package com.github.yoojia.web

import com.github.yoojia.web.core.Module
import java.text.SimpleDateFormat

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
abstract class LoggerModule : Module{

    companion object {

        internal val LOGGING_TEXT_NAME = "<next-web::logging:request:text.key>"
        internal val LOGGING_ENABLED_NAME = "<next-web::logging:request:enabled.key>"
        internal val FORMATTER = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")

        internal fun addLine(name: String, value: Any, buff: StringBuilder) {
            buff.append(name).append(": ").append(value).append("\r\n")
        }

        internal fun addSubLine(name: String, value: Any, buff: StringBuilder) {
            buff.append("    ").append(name).append(": ").append(value).append("\r\n")
        }
    }
}