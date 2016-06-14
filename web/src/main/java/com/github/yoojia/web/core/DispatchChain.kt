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

    fun add(module: Module) {
        modules.add(module)
    }

    @Throws(Exception::class)
    fun process(request: Request, response: Response) {
        next(request, response, this)
        threadedIndex.set(0)
    }

    @Throws(Exception::class)
    fun next(request: Request, response: Response, chain: DispatchChain) {
        val index = threadedIndex.get()
        if (index != modules.size) {
            threadedIndex.set(index + 1)
            modules[index].process(request, response, chain)
        }
    }

    fun clear(){
        modules.clear()
    }

    companion object {

        private val threadedIndex = object : ThreadLocal<Int>() {
            override fun initialValue(): Int {
                return 0
            }
        }

    }
}