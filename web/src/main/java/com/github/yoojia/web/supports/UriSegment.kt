package com.github.yoojia.web.supports

import kotlin.reflect.KClass

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.2
 */
class UriSegment(segment: String) {

    val isDynamic: Boolean
    val isWildcard: Boolean
    val type: KClass<*>
    val name: String

    init {
        isDynamic = segment.length >= 3 /* {a} */&& segment.startsWith("{") && segment.endsWith("}")
        isWildcard = !isDynamic && "*".equals(segment)
        val parseName = fun(offset: Int, segment: String): String {
            return segment.substring(offset, segment.length - 1).trim()
        }
        when{
            isDynamic && segment.startsWith("int:") -> {
                type = Long::class
                name = parseName(5, segment)
            }
            isDynamic && segment.startsWith("float:") -> {
                type = Double::class
                name = parseName(7, segment)
            }
            isDynamic && segment.startsWith("string:") -> {
                type = String::class
                name = parseName(8, segment)
            }
            else -> {
                type = String::class
                name = parseName(1, segment)
            }
        }
    }
}