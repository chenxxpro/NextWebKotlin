package com.github.yoojia.web.supports

import com.github.yoojia.web.util.splitUri
import java.util.*

/**
 * 客户端请求的元素封装类
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class RequestWrapper {

    // HTTP method
    val method: String

    // HTTP request uri segments
    val segments: List<UriSegment>

    // HTTP request uri
    val path: String

    private constructor(method: String, path: String, segments: List<UriSegment>) {
        this.method = method.toUpperCase()
        this.path = path
        this.segments = segments
    }

    companion object {
        /// 用于对客户端请求的封装
        fun request(method: String, uri: String, segments: List<String>): RequestWrapper {
            return RequestWrapper(method, uri, segments.map { seg -> UriSegment(seg) })
        }
        /// 用户对开发程序定义的封装
        fun define(method: String, uri: String): RequestWrapper {
            return RequestWrapper(method, uri, splitUri(uri).map { seg -> UriSegment(seg) })
        }

    }

    /**
     * 注意顺序：客户端的RequestWrapper与开发者定义的RequestWrapper进行匹配。
     */
    fun isRequestMatchDefine(define: RequestWrapper): Boolean {
        val request = this
        // 定义的HTTP方法为ALL，可以匹配所有HTTP方法
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
        return "{method: $method, path: $path}"
    }
}