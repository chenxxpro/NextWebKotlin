package com.github.yoojia.web.supports

import com.github.yoojia.web.Request
import com.github.yoojia.web.RequestChain
import com.github.yoojia.web.Response
import java.lang.reflect.Method
import kotlin.reflect.KClass

internal fun findAnnotated(hostType: Class<*>, action: (Method, String, String) -> Unit) {
    val ifAnnotated = fun(method: Method, type: KClass<out Annotation>) {
        if(method.isAnnotationPresent(type.java)) {
            val annotation = method.getAnnotation(type.java)
            when(annotation) {
                is GETPOST -> {
                    if(method.isAnnotationPresent(GET::class.java) || method.isAnnotationPresent(POST::class.java)) {
                        throw IllegalArgumentException("When @GETPOST declared, @GET/@POST is not allow declare in $method")
                    }
                    val path = method.getAnnotation(GETPOST::class.java).value
                    action.invoke(method, "GET", path)
                    action.invoke(method, "POST", path)
                }
                is GET -> action.invoke(method, "GET", annotation.value)
                is POST -> action.invoke(method, "POST", annotation.value)
                is PUT -> action.invoke(method, "PUT", annotation.value)
                is DELETE -> action.invoke(method, "DELETE", annotation.value)
                is ALL -> action.invoke(method, "ALL", annotation.value)
            }
        }
    }
    hostType.declaredMethods.forEach { method ->
        if(!method.isBridge && !method.isSynthetic) {
            ifAnnotated(method, GETPOST::class)
            ifAnnotated(method, GET::class)
            ifAnnotated(method, POST::class)
            ifAnnotated(method, PUT::class)
            ifAnnotated(method, DELETE::class)
            ifAnnotated(method, ALL::class)
        }
    }
}

internal fun checkReturnType(method: Method) {
    if(!Void.TYPE.equals(method.returnType)) {
        throw IllegalArgumentException("Return type of @GET/@POST/@PUT/@DELETE methods must be <Java::void> or <Kotlin::unit> !")
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
fun getRequestPriority(request: Comparator): Int {
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