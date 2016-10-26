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
class JavaMethodInvoker(@JvmField val hostType: Class<*>,
                        @JvmField val method: Method,
                        private val strictAccessible: Boolean,
                        private val argumentTypes: Array<Class<*>> = method.parameterTypes) {

    @Throws(Exception::class)
    fun invoke(request: Request, response: Response, chain: RequestChain, hostObject: Any) {
        if (strictAccessible) {
            strictAccessibleInvoke(request, response, chain, hostObject)
        }else{
            weekAccessibleInvoke(request, response, chain, hostObject)
        }
    }

    private fun weekAccessibleInvoke(request: Request, response: Response, chain: RequestChain, hostObject: Any){
        if(!method.isAccessible) {
            method.isAccessible = true
        }
        method.invoke(hostObject, *varargs(request, response, chain))
    }

    private fun strictAccessibleInvoke(request: Request, response: Response, chain: RequestChain, hostObject: Any) {
        val accessChanged: Boolean
        val originAccessible: Boolean
        originAccessible = method.isAccessible
        if(originAccessible != true) {
            accessChanged = true
            method.isAccessible = true
        }else{
            accessChanged = false
        }
        try{
            method.invoke(hostObject, *varargs(request, response, chain))
        }finally{
            if(accessChanged) {
                method.isAccessible = originAccessible
            }
        }
    }

    private fun varargs(request: Request, response: Response, chain: RequestChain): Array<Any> {
        val output = ArrayList<Any>()
        argumentTypes.forEach { type ->
            when(type) {
                Request::class.java -> output.add(request)
                Response::class.java -> output.add(response)
                RequestChain::class.java -> output.add(chain)
            }
        }
        return output.toArray()
    }

    override fun toString(): String{
        return "${hostType.name}.${method.name}()"
    }

}