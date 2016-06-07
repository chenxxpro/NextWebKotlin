package com.github.yoojia.web.http

/**
 * Http Controller
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.2
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Controller(
        /**
         * 基础地址。
         */
        val value: String = "/"
)