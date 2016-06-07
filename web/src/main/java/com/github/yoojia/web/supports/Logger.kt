package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
class Logger private constructor() {

    companion object {

        private val verbose: Boolean by lazy {
            "true".equals(System.getProperty("next-web.logger.verbose", "true"))
        }

        private val veryVerbose: Boolean by lazy {
            "true".equals(System.getProperty("next-web.logger.very-verbose", "true"))
        }

        fun v(message: String) {
            if(verbose) {
                println("V [${Thread.currentThread().id}] $message")
            }
        }

        fun vv(message: String) {
            if(verbose && veryVerbose) {
                println("Vv [${Thread.currentThread().id}] $message")
            }
        }

        fun d(message: String) {
            println("D [${Thread.currentThread().id}] $message")
        }

        fun w(message: String) {
            error("W [${Thread.currentThread().id}] $message")
        }

        fun e(message: String) {
            error("E [${Thread.currentThread().id}] $message")
        }

        fun e(error: Throwable) {
            error.printStackTrace()
            error("E [${Thread.currentThread().id}] $error")
        }

    }

}