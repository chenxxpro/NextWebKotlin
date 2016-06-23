package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class InternalPriority private constructor() {

    companion object {
        @JvmField val BEFORE_INTERCEPTOR = -1000
        @JvmField val AFTER_INTERCEPTOR = 1000
        @JvmField val HTTP = 0
        @JvmField val ASSETS = HTTP - 200
        @JvmField val VELOCITY = HTTP + 200
        @JvmField val INVALID = Int.MIN_VALUE
    }

}