package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Route(
        /**
         * 定义路由方法处理的URI地址。
         * 地址可以是以下三种方式:
         * - 静态地址: "/articles"
         * - 动态地址: "/users/{user_id}"
         */
        val path: String,

        /**
         * 接受处理的Http方法，默认为ALL。
         * 允许的HTTP方法：{ "ALL" , "GET", "POST", "DELETE", "PUT" }
         */
        val methods: Array<String> = arrayOf("all")
)