package com.github.yoojia.web.supports

import com.github.yoojia.web.util.splitToArray
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class Comparator {

    val method: String
    val segments: List<UriSegment>
    val path: String

    private constructor(method: String, path: String, segments: List<UriSegment>) {
        this.method = method.toUpperCase()
        this.path = path
        this.segments = segments
    }

    companion object {

        fun createRequest(method: String, uri: String, segments: List<String>): Comparator {
            return Comparator(method, uri, segments.map { createRequestUriSegment(it) })
        }

        fun createDefine(method: String, uri: String): Comparator {
            return Comparator(method, uri, splitToArray(uri).map { createDefineUriSegment(it) })
        }

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

    override fun toString(): String {
        return "$method $path"
    }
}