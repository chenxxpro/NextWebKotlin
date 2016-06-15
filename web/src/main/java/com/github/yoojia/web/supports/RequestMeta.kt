package com.github.yoojia.web.supports

import com.github.yoojia.web.util.splitUri

/**
 * 客户端请求的元素封装类
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class RequestMeta {

    // HTTP method
    val method: String

    // HTTP request uri segments
    val uriSegments: List<String>

    // HTTP request uri
    val path: String

    private constructor(method: String, path: String, segments: List<String>) {
        this.method = method.toUpperCase()
        this.path = path
        this.uriSegments = segments
    }

    companion object {

        fun forClient(method: String, uri: String, segments: List<String>): RequestMeta {
            return RequestMeta(method, uri, segments)
        }

        fun forDefine(method: String, uri: String): RequestMeta {
            return RequestMeta(method, uri, splitUri(uri))
        }

    }

    override fun toString(): String {
        return "{method: $method, uri: $path}"
    }
}