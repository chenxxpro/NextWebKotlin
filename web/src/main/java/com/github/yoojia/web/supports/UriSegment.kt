package com.github.yoojia.web.supports

import kotlin.reflect.KClass

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.2
 */
class UriSegment(segment: String) {

    val dynamic: Boolean
    val wildcard: Boolean
    val type: KClass<*>
    val name: String

    init {
        dynamic = segment.length >= 3/*{a}*/&& segment.startsWith("{") && segment.endsWith("}")

        wildcard = !dynamic && "*".equals(segment)

        val parseName = fun(offset: Int, segment: String): String {
            return segment.substring(offset, segment.length - 1).trim()
        }
        when{
            dynamic && segment.startsWith("int:") -> {
                type = Long::class
                name = parseName(5, segment)
            }
            dynamic && segment.startsWith("float:") -> {
                type = Double::class
                name = parseName(7, segment)
            }
            dynamic && segment.startsWith("string:") -> {
                type = String::class
                name = parseName(8, segment)
            }
            else -> {
                type = String::class
                if(dynamic) {
                    name = parseName(1, segment)
                }else{
                    name = segment
                }
            }
        }
    }

    companion object {

        private fun match(request: List<UriSegment>, define: List<UriSegment>): Boolean{
            for(i in request.indices) {
                val def = define[i]
                val match = def.dynamic || def.name.equals(request[i].name)
                if(!match) {
                    return false
                }
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