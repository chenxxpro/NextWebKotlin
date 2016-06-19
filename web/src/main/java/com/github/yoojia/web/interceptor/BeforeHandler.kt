package com.github.yoojia.web.interceptor

import com.github.yoojia.web.supports.InternalPriority
import com.github.yoojia.web.supports.ModuleHandler

/**
 * Http请求拦截器,后拦截器
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class BeforeHandler(classes: List<Class<*>>) :
        ModuleHandler("BeforeInterceptor", BeforeInterceptor::class.java, classes) {

    override fun getModuleUri(hostType: Class<*>): String {
        return hostType.getAnnotation(BeforeInterceptor::class.java).base
    }

    companion object {
        val DEFAULT_PRIORITY = InternalPriority.BEFORE_INTERCEPTOR
    }
}