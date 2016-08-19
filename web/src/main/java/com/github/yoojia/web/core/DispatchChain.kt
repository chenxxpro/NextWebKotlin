package com.github.yoojia.web.core

import com.github.yoojia.web.Request
import com.github.yoojia.web.Response
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
class DispatchChain {

    private val modules = ArrayList<Module>()
    private val threadIndex = object : ThreadLocal<Int>() {
        override fun initialValue(): Int? {
            return 0
        }
    }// Supported to Java 7

    private val moduleDeep: Int by lazy { modules.size }

    fun add(module: Module) {
        modules.add(module)
    }

    @Throws(Exception::class)
    fun route(request: Request, response: Response) {
        try{
            next(request, response, this)
        }finally{
            threadIndex.set(0/* reset */)
        }
    }

    @Throws(Exception::class)
    fun next(request: Request, response: Response, chain: DispatchChain) {
        val i = threadIndex.get()
        if (i != moduleDeep) {
            threadIndex.set(i + 1)//!! Set next index before process
            modules[i].process(request, response, chain)
        }
    }

    fun clear(){
        modules.clear()
    }

}