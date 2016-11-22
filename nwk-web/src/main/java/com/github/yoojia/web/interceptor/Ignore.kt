package com.github.yoojia.web.interceptor

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.10
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Ignore(val value: Array<String>)