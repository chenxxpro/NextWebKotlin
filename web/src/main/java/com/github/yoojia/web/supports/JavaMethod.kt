package com.github.yoojia.web.supports

import com.github.yoojia.web.Request
import com.github.yoojia.web.RequestChain
import com.github.yoojia.web.Response
import java.lang.reflect.Method

/**
 * 处理请求的Java方法定义
 */
data class JavaMethodDefine(val processor: JavaMethodProcessor, val request: HttpRequestDefine, val priority: Int = getRequestPriority(request)) {
    override fun toString(): String {
        return "{processor: $processor, request: $request, priority: $priority}"
    }
}

/**
 * 请求参数定义
 */
class HttpRequestDefine {

    val method: String
    val uriSegments: List<String>
    val uri: String

    constructor(method: String, uri: String, segments: List<String>) {
        this.method = method.toUpperCase()
        this.uri = uri
        this.uriSegments = segments
    }

    constructor(method: String, uri: String): this(method, uri, splitUri(uri))

    override fun toString(): String {
        return "{method: $method, uri: $uri}"
    }
}

/**
 * 查找@GET / @POST / @PUT / @DELETE 的方法函数列表
 */
fun filterAnnotatedMethods(hostType: Class<*>, processor: (Method, Class<out Annotation>) -> Unit) {
    val ifFindAnnotated = fun(method: Method, annotationType: Class<out Annotation>) {
        if(method.isAnnotationPresent(annotationType)) {
            processor.invoke(method, annotationType)
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
        throw IllegalArgumentException("Return type of @GET/@POST/@PUT/@DELETE methods must be <VOID>/<Unit> !")
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
fun createMethodDefine(rootUri: String, moduleType: Class<*>, method: Method, annotationType: Class<out Annotation>): JavaMethodDefine {
    val annotation = method.getAnnotation(annotationType)
    val params = when(annotation) {
        is GET -> Pair("GET", annotation.value)
        is POST -> Pair("POST", annotation.value)
        is PUT -> Pair("PUT", annotation.value)
        is DELETE -> Pair("DELETE", annotation.value)
        is ALL -> Pair("ALL", annotation.value)
        else -> throw IllegalArgumentException("Unexpected annotation <$annotation> in method: $method")
    }
    return JavaMethodDefine(JavaMethodProcessor(moduleType, method),
            HttpRequestDefine(params.first/*method*/, linkUri(rootUri, params.second/*path*/)))
}

/**
 * 计算请求参数的优先级
 * - 短URI路径优先；
 * - 静态方法优先；
 */
private fun getRequestPriority(define: HttpRequestDefine): Int {
    var priority = 0
    define.uriSegments.forEach {
        if(isWildcards(it)) {
            priority += -1
        }else{
            priority += if(isDynamicSegment(it)) 1 else 0
        }
    }
    return define.uriSegments.size + priority
}