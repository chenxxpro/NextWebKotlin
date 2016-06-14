package com.github.yoojia.web.supports

import com.github.yoojia.web.util.splitUri

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class HttpRequestDefine {

    val method: String
    val uriSegments: List<String>
    val uri: String

    constructor(method: String, uri: String, segments: List<String>) {
        this.method = method.toUpperCase()
        this.uri = uri
        this.uriSegments = segments
    }

    constructor(method: String, uri: String): this(method, uri, splitUri(uri))

    override fun toString(): String {
        return "{method: $method, uri: $uri}"
    }
}