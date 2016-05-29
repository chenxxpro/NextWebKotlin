package com.github.yoojia.web.util

import com.github.yoojia.web.Request
import com.github.yoojia.web.RequestChain
import com.github.yoojia.web.Response
import com.github.yoojia.web.supports.MethodProcessor
import com.github.yoojia.web.supports.Route
import java.lang.reflect.Method

data class MethodDefine(val processor: MethodProcessor, val request: RequestDefine, val priority: Int = getPriority(request))

/**
 * 请求参数定义
 */
class RequestDefine {

    val methods: List<String>
    val segments: List<String>
    val uri: String

    constructor(methods: List<String>, segments: List<String>) {
        this.methods = methods.map { it.toLowerCase() }
        this.segments = segments
        if(segments.size == 1) {
            this.uri = segments[0]
        }else{
            this.uri = segments.subList(1, segments.size - 1).joinToString()
        }
    }

    constructor(methods: List<String>, uri: String){
        this.methods = methods.map { it.toLowerCase() }
        this.uri = uri
        this.segments = splitUri(uri)
    }

    override fun toString(): String {
        return "${this.javaClass.simpleName}(methods=$methods, uri=$uri)"
    }
}

/**
 * 查找@Route方法列表
 */
fun filterRouteAnnotated(hostType: Class<*>, filterStep: (Method) -> Unit) {
    hostType.declaredMethods.filter {
        !it.isBridge && !it.isSynthetic &&
                it.isAnnotationPresent(Route::class.java)
    }.forEach(filterStep)
}

/**
 * 检查@Route方法的返回类型
 */
fun checkReturnType(method: Method) {
    if(!Void.TYPE.equals(method.returnType)) {
        throw IllegalArgumentException("Return type of @Route methods must be <VOID>/<Unit> !")
    }
}

/**
 * 检查@Route方法的参数要求
 */
fun checkArguments(method: Method) {
    val types = method.parameterTypes
    if(types.size !in 1..3) {
        throw IllegalArgumentException("@Route method must has 1..3 params, was ${types.size} in method $method")
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
 * 创建@Route方法的定义参数
 */
fun createMethodDefine(rootUri: String, moduleType: Class<*>, method: Method): MethodDefine {
    val route = method.getAnnotation(Route::class.java)
    val methods = route.methods.map { it.toLowerCase() }
    if(route.path.isEmpty()) {
        throw IllegalArgumentException("@Route.path is required !")
    }
    return MethodDefine(MethodProcessor(moduleType, method),
            RequestDefine(methods, linkUri(rootUri, route.path)))
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