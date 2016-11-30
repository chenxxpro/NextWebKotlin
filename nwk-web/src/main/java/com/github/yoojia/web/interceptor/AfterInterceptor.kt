package com.github.yoojia.web.interceptor

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class AfterInterceptor(
        /**
         * 模块基础地址。
         */
        val base: String = "/"
)