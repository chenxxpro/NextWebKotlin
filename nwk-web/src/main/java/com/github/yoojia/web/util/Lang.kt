package com.github.yoojia.web.util

import java.util.*
import kotlin.reflect.KClass

fun now(): Long {
    return System.currentTimeMillis()
}

fun escape(start: Long): Long {
    return now() - start
}

fun <E> List<E>.concat(b: List<E>): List<E> {
    val out = ArrayList<E>(this)
    out.addAll(b)
    return out.toList()
}

fun checkObjectType(value: Any, type: KClass<*>) {
    if(! type.javaObjectType.isAssignableFrom(value.javaClass)) {
        throw IllegalArgumentException("Unexpected type, expected: <${type.java}>, was: <${value.javaClass}>")
    }
}