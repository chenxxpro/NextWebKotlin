package com.github.yoojia.web.supports

import com.github.yoojia.web.util.splitUri

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class RequestMeta {

    val method: String
    val uriSegments: List<String>
    val uri: String

    private constructor(method: String, uri: String, segments: List<String>) {
        this.method = method.toUpperCase()
        this.uri = uri
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
        return "{method: $method, uri: $uri}"
    }
}