package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class InternalPriority private constructor() {

    companion object {
        val BEFORE_INTERCEPTOR = -1000
        val AFTER_INTERCEPTOR = 1000
        val HTTP = 0
        val UPLOADS = HTTP - 100
        val ASSETS = HTTP - 200
        val DOWNLOADS = HTTP + 100
        val VELOCITY = HTTP + 200
    }

}