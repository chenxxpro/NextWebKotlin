package com.github.yoojia.web

/**
 * 请求处理链
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class RequestChain() {

    private var mInterrupt: Boolean = false
    private var mStopDispatching: Boolean = false

    /**
     * 中断模块内处理接口之间的后续处理
     */
    fun interrupt() {
        mInterrupt = true
    }

    /**
     * 中断请求后续处理
     */
    fun stopDispatching() {
        mStopDispatching = true
    }

    fun isStopDispatching(): Boolean {
        return mStopDispatching
    }

    fun isInterrupted(): Boolean {
        return mInterrupt
    }
}