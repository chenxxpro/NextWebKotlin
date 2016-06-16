package com.github.yoojia.web.interceptor

import com.github.yoojia.web.supports.InternalPriority
import com.github.yoojia.web.supports.ModuleHandler

/**
 * Http请求拦截器，前拦截器
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class AfterHandler(classes: List<Class<*>>) :
        ModuleHandler("AfterInterceptor", AfterInterceptor::class.java, classes) {

    override fun getModuleConfigUri(hostType: Class<*>): String {
        return hostType.getAnnotation(AfterInterceptor::class.java).base
    }

    companion object {
        val DEFAULT_PRIORITY = InternalPriority.AFTER_INTERCEPTOR
    }
}