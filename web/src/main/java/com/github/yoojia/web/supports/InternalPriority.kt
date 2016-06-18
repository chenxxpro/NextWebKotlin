package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class InternalPriority private constructor() {

    companion object {
        @JvmStatic val BEFORE_INTERCEPTOR = -1000
        @JvmStatic val AFTER_INTERCEPTOR = 1000
        @JvmStatic val HTTP = 0
        @JvmStatic val ASSETS = HTTP - 200
        @JvmStatic val VELOCITY = HTTP + 200
        @JvmStatic val INVALID = Int.MIN_VALUE
    }

}