package com.github.yoojia.web.interceptor

import com.github.yoojia.web.supports.InternalPriority

/**
 * Http请求拦截器,后拦截器
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class BeforeHandler(classes: List<Class<*>>) :
        InterceptorHandler("BeforeInterceptor", BeforeInterceptor::class.java, classes) {

    override fun getRootUri(hostType: Class<*>): String {
        return hostType.getAnnotation(BeforeInterceptor::class.java).base
    }

    companion object {
        @JvmField val DEFAULT_PRIORITY = InternalPriority.INTERCEPTOR_BEFORE
    }
}