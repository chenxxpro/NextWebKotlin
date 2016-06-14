package com.github.yoojia.web.supports

import com.github.yoojia.web.util.isDynamicSegment
import com.github.yoojia.web.util.isWildcards
import java.lang.reflect.Method


/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class MethodMeta(
        val processor: JavaMethodProcessor,
        val request: RequestMeta,
        val priority: Int = getRequestPriority(request)) {

    val javaMethod: Method by lazy {
        processor.method
    }

    override fun toString(): String {
        return "{processor: $processor, request: $request, priority: $priority}"
    }

    companion object {

        private fun getRequestPriority(meta: RequestMeta): Int {
            /**
             * 计算请求参数的优先级
             * - 首先使用用户设置自定义优先级(非 InternalPriority.INVALID 值)
             * - 短URI路径优先；
             * - 静态方法优先；
             */
            var priority = 0
            meta.uriSegments.forEach {
                if(isWildcards(it)) {
                    priority += -1
                }else{
                    priority += if(isDynamicSegment(it)) 1 else 0
                }
            }
            return meta.uriSegments.size + priority
        }
    }
}