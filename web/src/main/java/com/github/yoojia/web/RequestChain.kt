package com.github.yoojia.web

/**
 * 请求处理链
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class RequestChain() {

    private var interrupt: Boolean = false
    private var stopDispatching: Boolean = false

    /**
     * 中断模块内处理接口之间的后续处理
     */
    fun interrupt() {
        interrupt = true
    }

    /**
     * 中断请求后续处理
     */
    fun stopDispatching() {
        stopDispatching = true
    }

    fun isStopDispatching(): Boolean {
        return stopDispatching
    }

    fun isInterrupted(): Boolean {
        return interrupt
    }
}