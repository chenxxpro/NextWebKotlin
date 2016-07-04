package com.github.yoojia.web.supports

import com.github.yoojia.web.Request
import com.github.yoojia.web.RequestChain
import com.github.yoojia.web.Response
import java.lang.reflect.Method

internal fun findAnnotated(hostType: Class<*>, action: (Method, Class<out Annotation>) -> Unit) {
    val ifAnnotated = fun(method: Method, annotationType: Class<out Annotation>) {
        if(method.isAnnotationPresent(annotationType)) {
            action.invoke(method, annotationType)
        }
    }
    hostType.declaredMethods.forEach { method ->
        if(!method.isBridge && !method.isSynthetic) {
            // 允许一个方法定义多种HTTP Method
            ifAnnotated(method, GET::class.java)
            ifAnnotated(method, POST::class.java)
            ifAnnotated(method, PUT::class.java)
            ifAnnotated(method, DELETE::class.java)
            ifAnnotated(method, ALL::class.java)
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
        throw IllegalArgumentException("@GET/@POST/@PUT/@DELETE methods must has 1 to 3 params, was ${types.size} in method $method")
    }
    val marks = arrayOf(false, false, false)
    val duplicate = fun (type: Class<*>, index: Int) {
        if(marks[index]) {
            throw IllegalArgumentException("Duplicate arguments type <$type> in method $method")
        }
        marks[index] = true
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
fun getRequestPriority(request: RequestWrapper): Int {
    var priority = request.segments.size
    request.segments.forEach { segment ->
        if(segment.isWildcard) {
            priority += -1
        }else{
            priority += if(segment.isDynamic) {if(segment.isFixedType) {1} else {2}} else {0}
        }
    }
    return priority
}