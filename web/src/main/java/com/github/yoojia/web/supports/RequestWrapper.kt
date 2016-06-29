package com.github.yoojia.web.supports

import com.github.yoojia.web.util.splitToArray
import java.util.*

/**
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

        fun createFromClient(method: String, uri: String, segments: List<String>): RequestWrapper {
            return RequestWrapper(method, uri, segments.map {
                seg -> createRequestUriSegment(seg)
            })
        }

        fun createFromDefine(method: String, uri: String): RequestWrapper {
            return RequestWrapper(method, uri, splitToArray(uri).map {
                seg -> createDefineUriSegment(seg)
            })
        }

    }

    fun isRequestMatchDefine(define: RequestWrapper): Boolean {
        val request = this
        if("ALL".equals(define.method)) {
            return isUriSegmentMatch(requests = request.segments, defines = define.segments)
        }else{
            return request.method.equals(define.method)
            && isUriSegmentMatch(requests = request.segments, defines = define.segments)
        }
    }

    /**
     * 从指定URI请求资源中，解析出动态参数
     */
    fun parseDynamic(sources: List<String>): Map<String, String> {
        val output = HashMap<String, String>()
        for(i in segments.indices) {
            val segment = segments[i]
            if(segment.isDynamic) {
                output.put(segment.segment, sources[i])
            }
        }
        return if(output.isEmpty()) emptyMap() else output
    }

    override fun toString(): String {
        return "$method $path"
    }
}