package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
interface Filter<T> {

    fun accept(value: T): Boolean

}