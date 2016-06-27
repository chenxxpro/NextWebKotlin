package com.github.yoojia.web.supports

import com.github.yoojia.web.Request
import com.github.yoojia.web.RequestChain
import com.github.yoojia.web.Response
import java.lang.reflect.Method

internal fun annotatedMethods(hostType: Class<*>, action: (Method, Class<out Annotation>) -> Unit) {
    val ifFindAnnotated = fun(method: Method, annotationType: Class<out Annotation>) {
        if(method.isAnnotationPresent(annotationType)) {
            action.invoke(method, annotationType)
        }
    }
    hostType.declaredMethods.forEach {
        if(!it.isBridge && !it.isSynthetic) {
            // 允许一个方法定义多种HTTP Method
            ifFindAnnotated(it, GET::class.java)
            ifFindAnnotated(it, POST::class.java)
            ifFindAnnotated(it, PUT::class.java)
            ifFindAnnotated(it, DELETE::class.java)
            ifFindAnnotated(it, ALL::class.java)
        }
    }
}

internal fun checkReturnType(method: Method) {
    if(!Void.TYPE.equals(method.returnType)) {
        throw IllegalArgumentException("Return type of @GET/@POST/@PUT/@DELETE methods must be <VOID> or <UNIT> !")
    }
}

internal fun checkArguments(method: Method) {
    val types = method.parameterTypes
    if(types.size !in 1..3) {
        throw IllegalArgumentException("@GET/@POST/@PUT/@DELETE methods must has 1..3 params, was ${types.size} in method $method")
    }
    val used = arrayOf(false, false, false)
    val duplicate = fun (type: Class<*>, index: Int) {
        if(used[index]) {
            throw IllegalArgumentException("Duplicate arguments type <$type> in method $method")
        }
        used[index] = true
    }
    types.forEach { type ->
        when {
            type.equals(Request::class.java) -> duplicate(type, 0)
            type.equals(Response::class.java) -> duplicate(type, 1)
            type.equals(RequestChain::class.java) -> duplicate(type, 2)
            else -> throw IllegalArgumentException("Unsupported argument type <$type> in method $method")
        }
    }
}

/**
 * 计算请求参数的优先级
 * - 短路径优先；
 * - 静态方法优先；
 * - 动态参数：固定参数类型{int:user_id}优先于不定参数类型{user_id}
 */
fun getRequestPriority(wrapper: RequestWrapper): Int {
    var priority = wrapper.segments.size
    wrapper.segments.forEach { segment ->
        if(segment.wildcard) {
            priority += -1
        }else{
            priority += if(segment.dynamic) {if(segment.fixedType) {1} else {2}} else {0}
        }
    }
    return priority
}