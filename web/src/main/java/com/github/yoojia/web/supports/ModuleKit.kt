package com.github.yoojia.web.supports

import com.github.yoojia.web.Request
import com.github.yoojia.web.RequestChain
import com.github.yoojia.web.Response
import java.lang.reflect.Method

/**
 * 查找@GET/POST/PUT/DELETE 的方法函数列表
 */
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

/**
 * 检查方法的返回类型
 */
internal fun checkReturnType(method: Method) {
    if(!Void.TYPE.equals(method.returnType)) {
        throw IllegalArgumentException("Return type of @GET/@POST/@PUT/@DELETE methods must be <VOID>/<Unit> !")
    }
}

/**
 * 检查方法的参数要求
 */
internal fun checkArguments(method: Method) {
    val types = method.parameterTypes
    if(types.size !in 1..3) {
        throw IllegalArgumentException("@GET/@POST/@PUT/@DELETE methods must has 1..3 params, but was ${types.size} in method $method")
    }
    val used = arrayOf(false, false, false)
    val checkDuplicate = fun (type: Class<*>, index: Int) {
        if(used[index]) {
            throw IllegalArgumentException("Duplicate arguments type <$type> in method $method")
        }
        used[index] = true
    }
    types.forEach { type ->
        when {
            type.equals(Request::class.java) -> checkDuplicate(type, 0)
            type.equals(Response::class.java) -> checkDuplicate(type, 1)
            type.equals(RequestChain::class.java) -> checkDuplicate(type, 2)
            else -> throw IllegalArgumentException("Unsupported argument type <$type> in method $method")
        }
    }
}

/**
 * 计算请求参数的优先级
 * - 首先使用用户设置自定义优先级(非 InternalPriority.INVALID 值)
 * - 短URI路径优先；
 * - 静态方法优先；
 */
fun getRequestPriority(wrapper: RequestWrapper): Int {

    var priority = 0
    wrapper.segments.forEach { segment ->
        if(segment.wildcard) {
            priority += -1
        }else{
            priority += if(segment.dynamic) 1 else 0
        }
    }
    return wrapper.segments.size + priority
}