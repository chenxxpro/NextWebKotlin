package com.github.yoojia.web.supports

import com.github.yoojia.web.util.splitToArray
import java.util.*

/**
 * 客户端请求的元素封装类
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class RequestWrapper {

    val method: String
    val segments: List<UriSegment>
    val path: String

    private constructor(method: String, path: String, segments: List<UriSegment>) {
        this.method = method.toUpperCase()
        this.path = path
        this.segments = segments
    }

    companion object {

        fun request(method: String, uri: String, segments: List<String>): RequestWrapper {
            return RequestWrapper(method, uri, segments.map { seg -> UriSegment(seg, absoluteType = true/*请求参数的数值类型要求为绝对类型，不能为ValueType.Any*/) })
        }

        fun define(method: String, uri: String): RequestWrapper {
            return RequestWrapper(method, uri, splitToArray(uri).map { seg -> UriSegment(seg) })
        }

    }

    fun isRequestMatchDefine(define: RequestWrapper): Boolean {
        val request = this
        if("ALL".equals(define.method)) {
            return UriSegment.isRequestMatchDefine(request.segments, define.segments)
        }else{
            return request.method.equals(define.method)
            && UriSegment.isRequestMatchDefine(request.segments, define.segments)
        }
    }

    /**
     * 从指定URI请求资源中，解析出动态参数
     */
    fun parseDynamic(sources: List<String>): Map<String, String> {
        val out = HashMap<String, String>()
        for(i in segments.indices) {
            val seg = segments[i]
            if(seg.dynamic) {
                out.put(seg.name, sources[i])
            }
        }
        return if(out.isEmpty()) emptyMap() else out
    }

    override fun toString(): String {
        return "$method $path"
    }
}