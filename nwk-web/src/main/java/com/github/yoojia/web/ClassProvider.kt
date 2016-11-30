package com.github.yoojia.web

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.1
 */
interface ClassProvider {

    fun getClasses(context: Context): List<Class<*>>
}