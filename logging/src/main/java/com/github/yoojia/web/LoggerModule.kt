package com.github.yoojia.web

import java.text.SimpleDateFormat

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
abstract class LoggerModule : Module {

    companion object {

        @JvmField internal val LOGGING_TEXT_NAME = "nwk.logging.request:text.key"
        @JvmField internal val LOGGING_ENABLED_NAME = "nwk.logging.request:enabled.key"
        @JvmField internal val FORMATTER = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")

        internal fun addLine(name: String, value: Any, buff: StringBuilder) {
            buff.append(name).append(": ").append(value).append("\r\n")
        }

        internal fun addSubLine(name: String, value: Any, buff: StringBuilder) {
            buff.append("    ").append(name).append(": ").append(value).append("\r\n")
        }
    }
}