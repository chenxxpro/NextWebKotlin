package com.github.yoojia.web.util

import com.github.yoojia.web.Request
import com.github.yoojia.web.RequestChain
import com.github.yoojia.web.Response
import com.github.yoojia.web.supports.*
import java.lang.reflect.Method

data class MethodDefine(val processor: MethodProcessor, val request: RequestDefine, val priority: Int = getPriority(request)) {
    override fun toString(): String {
        return "{processor: $processor, request: $request, priority: $priority}"
    }
}

/**
 * 请求参数定义
 */
class RequestDefine {

    val method: String
    val segments: List<String>
    val uri: String

    constructor(method: String, segments: List<String>) {
        this.method = method
        this.segments = segments
        if(segments.size == 1) {
            this.uri = segments[0]
        }else{
            this.uri = segments.subList(1, segments.size - 1).joinToString()
        }
    }

    constructor(method: String, uri: String){
        this.method = method
        this.uri = uri
        this.segments = splitUri(uri)
    }

    override fun toString(): String {
        return "{method: $method, uri: $uri}"
    }
}

/**
 * 查找@GET / @POST / @PUT / @DELETE 的方法函数列表
 */
fun filterAnnotatedMethods(hostType: Class<*>, eachStep: (Method, Class<out Annotation>) -> Unit) {
    val ifFindAnnotated = fun(method: Method, annotationType: Class<out Annotation>) {
        if(method.isAnnotationPresent(annotationType)) {
            eachStep.invoke(method, annotationType)
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
fun checkReturnType(method: Method) {
    if(!Void.TYPE.equals(method.returnType)) {
        throw IllegalArgumentException("Return type of @Route methods must be <VOID>/<Unit> !")
    }
}

/**
 * 检查方法的参数要求
 */
fun checkArguments(method: Method) {
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
 * 创建方法的定义参数
 */
fun createMethodDefine(rootUri: String, moduleType: Class<*>, method: Method, annotationType: Class<out Annotation>): MethodDefine {
    val annotation = method.getAnnotation(annotationType)
    val params = when(annotation) {
        is GET -> Pair("get", annotation.value)
        is POST -> Pair("post", annotation.value)
        is PUT -> Pair("put", annotation.value)
        is DELETE -> Pair("delete", annotation.value)
        is ALL -> Pair("all", annotation.value)
        else -> throw IllegalArgumentException("Unexpected annotation <$annotation> in method: $method")
    }
    return MethodDefine(MethodProcessor(moduleType, method),
            RequestDefine(params.first/*method*/, linkUri(rootUri, params.second/*path*/)))
}

/**
 * 计算请求参数的优先级
 * - 短URI路径优先；
 * - 静态方法优先；
 */
private fun getPriority(define: RequestDefine): Int {
    var priority = 0
    define.segments.forEach {
        if(isWildcards(it)) {
            priority += -1
        }else{
            priority += if(isDynamicSegment(it)) 1 else 0
        }
    }
    return define.segments.size + priority
}