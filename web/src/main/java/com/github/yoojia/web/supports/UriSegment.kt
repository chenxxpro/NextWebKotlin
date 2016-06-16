package com.github.yoojia.web.supports

import com.github.yoojia.web.util.valueType

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.2
 */
class UriSegment(segment: String) {

    val dynamic: Boolean
    val wildcard: Boolean
    val type: Class<*>
    val name: String

    init {
        dynamic = segment.length >= 3/*{a}*/&& segment.startsWith("{") && segment.endsWith("}")

        wildcard = !dynamic && "*".equals(segment)

        val parseName = fun(offset: Int, segment: String): String {
            return segment.substring(offset, segment.length - 1).trim()
        }
        when{
            dynamic && segment.startsWith("int:") -> {
                type = Long::class.java
                name = parseName(5, segment)
            }
            dynamic && segment.startsWith("float:") -> {
                type = Double::class.java
                name = parseName(7, segment)
            }
            dynamic && segment.startsWith("string:") -> {
                type = String::class.java
                name = parseName(8, segment)
            }
            else -> {
                type = String::class.java
                name = if(dynamic) parseName(1, segment) else segment
            }
        }
    }

    companion object {

        /**
         * 在UriSegment资源长度相同的情况下，判断它们是否匹配；
         * - 定义为动态参数：比较它们的类型是否相同，忽略资源名；定义为字符串类型时，可以匹配任意请求资源类型；
         * - 定义为静态字段：比较资源名是否相同（大小写完全相同）；
         */
        private fun match(requests: List<UriSegment>, defines: List<UriSegment>): Boolean{
            for(i in requests.indices) {
                val define = defines[i]
                val request = requests[i]
                val match: Boolean
                if(define.dynamic) {
                    match = String::class.java.equals(define.type) ||
                            define.type.equals(valueType(request.name))
                }else{
                    match = define.name.equals(request.name, ignoreCase = false)
                }
                if(!match) return false
            }
            return true
        }

        /**
         * 客户端请求的UriSegments与定义的UriSegments是否匹配。
         */
        fun isRequestMatchDefine(request: List<UriSegment>, define: List<UriSegment>): Boolean {
            if(define.last().wildcard) {
                val defineIndex = define.size - 1
                if(request.size < defineIndex) {
                    return false
                }else{
                    return match(request.subList(0, defineIndex), define.subList(0, defineIndex))
                }
            }else{
                return request.size == define.size && match(request, define)
            }
        }
    }
}