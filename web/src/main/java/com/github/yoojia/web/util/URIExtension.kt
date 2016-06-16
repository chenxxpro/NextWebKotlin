package com.github.yoojia.web.util

import java.net.URI
import java.util.*

/**
 * 将URI按指定分隔符分割成字符数组。如指定分隔符为"/"，将 "/users/yoojia" 拆分成数组\["users", "yoojia" \]。
 * 如果 separatorAppendToRoot 为 true, 则将分隔符插入在第一位，返回结果为 \["/","users", "yoojia"\]。
 */
fun splitToArray(uri: String, separator: Char, separatorAppendToRoot: Boolean = false): List<String> {
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
fun splitToArray(uri: String): List<String> {
    return splitToArray(uri, '/', true)
}

/**
 * 连接两个URI地址
 */
fun concatPath(root: String, path: String): String {
    val ends = root.endsWith("/")
    val starts = path.startsWith("/")
    return root + if(ends && starts) {
        path.substring(1)// Cut "/"
    }else{
        (if(ends || starts) "" else "/") + path
    }
}



