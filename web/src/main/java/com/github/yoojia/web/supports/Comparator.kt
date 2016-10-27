package com.github.yoojia.web.supports

import com.github.yoojia.web.util.splitToArray

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class Comparator {

    @JvmField val method: String
    @JvmField val segments: List<UriSegment>
    @JvmField val path: String

    private constructor(method: String, path: String, segments: List<UriSegment>) {
        this.method = method.toUpperCase()
        this.path = path
        this.segments = segments
    }

    fun isMatchDefine(define: Comparator): Boolean {
        val request = this
        if("ALL".equals(define.method)) {
            return isUriSegmentMatch(requests = request.segments, defines = define.segments)
        }else{
            return request.method.equals(define.method)
            && isUriSegmentMatch(requests = request.segments, defines = define.segments)
        }
    }

    companion object {

        fun createRequest(method: String, uri: String, segments: List<String>): Comparator {
            return Comparator(method, uri, segments.map { createRequestUriSegment(it) })
        }

        fun createDefine(method: String, uri: String): Comparator {
            return Comparator(method, uri, splitToArray(uri).map { createDefineUriSegment(it) })
        }

        /**
         * 客户端请求的UriSegments与定义的UriSegments是否匹配。
         */
        private fun isUriSegmentMatch(requests: List<UriSegment>, defines: List<UriSegment>): Boolean {
            if(defines.last().isWildcard) {
                val index = defines.size - 1
                if(requests.size < index) {
                    return false
                }else{
                    return match(requests.subList(0, index), defines.subList(0, index))
                }
            }else{
                return requests.size == defines.size && match(requests, defines)
            }
        }

        /**
         * 在UriSegment资源长度相同的情况下，判断它们是否匹配；
         * - 定义为动态参数：比较它们的类型是否相同，忽略资源名；定义为字符串类型时，可以匹配任意请求资源类型；
         * - 定义为静态字段：比较资源名是否相同（大小写完全相同）；
         */
        private fun match(requests: List<UriSegment>, defines: List<UriSegment>): Boolean{
            requests.forEachIndexed { i, request ->
                val define = defines[i]
                val match: Boolean
                if(define.dynamic) {
                    match = define.valueType.match(request.valueType)
                }else{
                    match = define.segment.equals(request.segment, ignoreCase = false)
                }
                if(!match) return false
            }
            return true
        }
    }

    override fun toString(): String {
        return "$method $path"
    }
}