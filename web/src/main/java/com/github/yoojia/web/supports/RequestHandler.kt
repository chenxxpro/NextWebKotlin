package com.github.yoojia.web.supports

import com.github.yoojia.web.util.concat
import java.lang.reflect.Method

/**
 * 为模块类中@GET/POST/PUT/DELETE等方法创建的请求处理器。
 * 对应每个Java method生成一个RequestHandler；
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class RequestHandler(
        val root: String,
        val invoker: JavaMethodInvoker,
        val comparator: Comparator,
        val priority: Int = getRequestPriority(comparator)) {

    val javaMethod: Method by lazy {
        invoker.method
    }

    override fun toString(): String {
        return "{invoker: $invoker, comparator: $comparator, priority: $priority}"
    }

    companion object {

        internal fun create(root: String, moduleType: Class<*>, javaMethod: Method, httpMethod: String, path: String): RequestHandler {
            return RequestHandler(root,
                    JavaMethodInvoker(moduleType, javaMethod),
                    Comparator.createDefine(httpMethod, concat(root, path)))
        }
    }
}