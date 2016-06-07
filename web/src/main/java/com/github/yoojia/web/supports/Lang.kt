package com.github.yoojia.web.supports

import java.util.*

/**
 * 当前时间，毫秒数
 */
fun now(): Long {
    return System.currentTimeMillis()
}

/**
 * 从指定时间到当前时间的毫秒差
 */
fun escape(start: Long): Long {
    return now() - start
}

/**
 * 连接两个List，返回一个新List
 */
fun <E> List<E>.concat(b: List<E>): List<E> {
    val out = ArrayList<E>(this)
    out.addAll(b)
    return out.toList()
}