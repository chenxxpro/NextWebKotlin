package com.github.yoojia.web

/**
 * 请求处理链
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class RequestChain() {

    internal var isInterrupted: Boolean = false

    fun interrupt() {
        isInterrupted = true
    }

}