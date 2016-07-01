package com.github.yoojia.web.supports

import com.github.yoojia.web.Request
import com.github.yoojia.web.RequestChain
import com.github.yoojia.web.Response
import java.lang.reflect.Method
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class JavaMethodInvoker(val hostType: Class<*>, val method: Method) {

    @Throws(Exception::class)
    fun invoke(request: Request, response: Response, chain: RequestChain, hostObject: Any) {
        val origin = method.isAccessible
        method.isAccessible = true
        method.invoke(hostObject, *varargs(request, response, chain))
        method.isAccessible = origin
    }

    private fun varargs(request: Request, response: Response, chain: RequestChain): Array<Any> {
        val output = ArrayList<Any>()
        method.parameterTypes.forEach { type ->
            when {
                type.equals(Request::class.java) -> output.add(request)
                type.equals(Response::class.java) -> output.add(response)
                type.equals(RequestChain::class.java) -> output.add(chain)
            }
        }
        return output.toArray()
    }

    override fun toString(): String{
        return "${hostType.name}.${method.name}()"
    }

}