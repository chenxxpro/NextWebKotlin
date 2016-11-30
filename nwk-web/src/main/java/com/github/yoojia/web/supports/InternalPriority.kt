package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
object InternalPriority {

    private val STEP_10K = 10*1000

    /**
     * 前日志记录优先级
     */
    @JvmField val LOGGING_BEFORE = -2 * STEP_10K

    /**
     * 前拦截器优先级
     */
    @JvmField val INTERCEPTOR_BEFORE = -1 * STEP_10K

    /**
     * 后拦截器优先级
     */
    @JvmField val INTERCEPTOR_AFTER = Math.abs(INTERCEPTOR_BEFORE)

    /**
     * 后日志记录优先级
     */
    @JvmField val LOGGING_AFTER = Math.abs(LOGGING_BEFORE)

    /**
     * HTTP处理模块优先级
     */
    @JvmField val HTTP = 0

    /**
     * 资源模块优先级
     */
    @JvmField val ASSETS = HTTP - STEP_10K

    /**
     * 模块处理模块优先级
     */
    @JvmField val TEMPLATES = HTTP + STEP_10K


}