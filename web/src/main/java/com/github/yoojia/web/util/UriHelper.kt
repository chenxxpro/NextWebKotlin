package com.github.yoojia.web.util

import com.github.yoojia.web.supports.RequestMeta
import java.util.*
import kotlin.reflect.KClass

/**
 * 将URI按指定分隔符分割成字符数组。如指定分隔符为"/"，将 "/users/yoojia" 拆分成数组\["users", "yoojia" \]。
 * 如果 separatorAppendToRoot 为 true, 则将分隔符插入在第一位，返回结果为 \["/","users", "yoojia"\]。
 */
fun splitUri(uri: String, separator: Char, separatorAppendToRoot: Boolean = false): List<String> {
    val out = ArrayList<String>()
    if (separatorAppendToRoot) {
        out.add(separator.toString())
    }
    var index = uri.indexOf(separator, 0)
    var preIndex = 0
    while(index != -1) {
        if (preIndex != index) {
            out.add(uri.substring(preIndex, index).trim())
        }
        index++
        preIndex = index
        index = uri.indexOf(separator, index)
    }
    if (preIndex < uri.length) {
        out.add(uri.substring(preIndex).trim())
    }
    return out.toList()
}

/**
 * 拆分URL地址
 */
fun splitUri(uri: String): List<String> {
    return splitUri(uri, '/', true)
}

/**
 * 连接两个URI地址
 */
fun linkUri(root: String, path: String): String {
    val ends = root.endsWith("/")
    val starts = path.startsWith("/")
    return root + if(ends && starts) {
        path.substring(1)// Cut "/"
    }else{
        (if(ends || starts) "" else "/") + path
    }
}

/**
 * 判断两个URI资源是否匹配
 */
fun isUriResourceMatched(request: List<String>, define: List<String>): Boolean{
    val arrayMatched = fun(request: List<String>, define: List<String>): Boolean {
        for(index in request.indices) {
            val def = define[index]
            val match = isDynamicSegment(def) || def.equals(request[index])
            if(!match) {
                return false
            }
        }
        return true
    }
    // 参数中定义了 * 表示接收其后所有字段的请求
    if(isWildcards(define.last())) {
        val defineIndex = define.size - 1
        if(request.size < defineIndex) {
            return false
        }else{
            return arrayMatched(request.subList(0, defineIndex), define.subList(0, defineIndex))
        }
    }else{
        return request.size == define.size && arrayMatched(request, define)
    }
}

/**
 * 判断两个请求是否匹配
 */
fun isRequestMatched(req: RequestMeta, meta: RequestMeta): Boolean {
    // 定义的HTTP方法为ALL，可以匹配所有HTTP方法
    if("ALL".equals(meta.method)) {
        return isUriResourceMatched(req.uriSegments, meta.uriSegments)
    }else{
        return req.method.equals(meta.method) &&
                isUriResourceMatched(req.uriSegments, meta.uriSegments)
    }
}

/**
 * 解析请求URI资源，返回动态参数
 */
fun dynamicParams(request: List<String>, meta: RequestMeta): Map<String, String> {
    val out = HashMap<String, String>()
    // request: ['/', 'users', 'yoojia']
    // define: ['/', 'users', '{username}']
    for(i in meta.uriSegments.indices) {
        val seg = meta.uriSegments[i]
        if(isDynamicSegment(seg)) {
            out.put(getDynamicSegmentName(seg), request[i])
        }
    }
    return if(out.isEmpty()) emptyMap() else out
}

/**
 * 是否是动态参数
 */
fun isDynamicSegment(segment: String): Boolean {
    return segment.length >= 3 /* {a} */&&
            segment.startsWith("{") && segment.endsWith("}")
}

fun getDynamicSegmentNameType(segment: String): Pair<String, KClass<*>> {
    /*
        {username} -> String
        {string:username} -> String
        {int:user-id} -> Int, Long
        {float:value} -> Float, Double
    */
    val getDynamicName = fun(offset: Int, segment: String): String {
        return segment.substring(offset, segment.length - 1).trim()
    }
    when{
        segment.startsWith("int:") -> return Pair(getDynamicName(5, segment), Long::class)
        segment.startsWith("float:") -> return Pair(getDynamicName(7, segment), Double::class)
        segment.startsWith("string:") -> return Pair(getDynamicName(8, segment), String::class)
        else -> return Pair(getDynamicName(1, segment), String::class)
    }
}

/**
 * 是否为通配符
 */
fun isWildcards(segment: String): Boolean {
    return "*".equals(segment)
}

private fun getDynamicSegmentName(segment: String): String {
    return segment.substring(1, segment.length - 1).trim()
}


