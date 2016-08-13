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
class JavaMethodInvoker(val hostType: Class<*>,
                        val method: Method,
                        private val argumentTypes: Array<Class<*>> = method.parameterTypes) {

    @Throws(Exception::class)
    fun invoke(request: Request, response: Response, chain: RequestChain, hostObject: Any) {
        val originFlag = method.isAccessible
        val accessChanged: Boolean
        if(originFlag != true) {
            accessChanged = true
            method.isAccessible = true
        }else{
            accessChanged = false
        }
        try{
            method.invoke(hostObject, *varargs(request, response, chain))
        }finally{
            if(accessChanged) {
                method.isAccessible = originFlag
            }
        }
    }

    private fun varargs(request: Request, response: Response, chain: RequestChain): Array<Any> {
        val output = ArrayList<Any>()
        argumentTypes.forEach { type ->
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