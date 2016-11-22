package com.github.yoojia.web.supports

import com.github.yoojia.web.util.concat
import java.lang.reflect.Method

/**
 * 为模块类中@GET/POST/PUT/DELETE等方法创建的请求处理器。
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class RequestHandler(
        val root: String,
        val invoker: JavaInvoker,
        val comparator: Comparator,
        val priority: Int = getRequestPriority(comparator)) {

    val javaMethod: Method by lazy { invoker.method }

    override fun toString(): String {
        return "{invoker: $invoker, comparator: $comparator, priority: $priority}"
    }

    companion object {

        internal fun create(root: String, moduleType: Class<*>, javaMethod: Method, httpMethod: String, path: String): RequestHandler {
            return RequestHandler(root,
                    JavaInvoker(moduleType, javaMethod, false/*Set Week Accessible by default*/),
                    Comparator.createDefine(httpMethod, concat(root, path)))
        }
    }
}