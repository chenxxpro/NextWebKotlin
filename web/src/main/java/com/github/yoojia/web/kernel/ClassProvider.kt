package com.github.yoojia.web.kernel

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.1
 */
interface ClassProvider {

    fun get(): List<Class<*>>
}