package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
object InternalPriority {

    /**
     * 前日志记录优先级
     */
    @JvmField val LOGGER_BEFORE = -20000

    /**
     * 前拦截器优先级
     */
    @JvmField val INTERCEPTOR_BEFORE = -10000

    /**
     * HTTP处理模块优先级
     */
    @JvmField val HTTP = 0

    /**
     * 资源模块优先级
     */
    @JvmField val ASSETS = HTTP - 500

    /**
     * 模块处理模块优先级
     */
    @JvmField val TEMPLATES = HTTP + 500

    /**
     * 后拦截器优先级
     */
    @JvmField val INTERCEPTOR_AFTER = Math.abs(INTERCEPTOR_BEFORE)

    /**
     * 后日志记录优先级
     */
    @JvmField val LOGGER_AFTER = Math.abs(LOGGER_BEFORE)

}