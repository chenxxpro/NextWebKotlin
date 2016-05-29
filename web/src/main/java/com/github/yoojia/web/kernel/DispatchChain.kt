package com.github.yoojia.web.kernel

import com.github.yoojia.web.Request
import com.github.yoojia.web.Response
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
class DispatchChain {

    private val mModuleProcessors = ArrayList<Module>()

    fun add(module: Module) {
        mModuleProcessors.add(module)
    }

    @Throws(Exception::class)
    fun process(request: Request, response: Response) {
        next(request, response, this)
        threadedIndex.set(0)
    }

    @Throws(Exception::class)
    fun next(request: Request, response: Response, chain: DispatchChain) {
        val index = threadedIndex.get()
        // 已历遍全部处理模块
        if (index == mModuleProcessors.size) {
            return
        }
        threadedIndex.set(index + 1)
        mModuleProcessors[index].process(request, response, chain)
    }

    fun clear(){
        mModuleProcessors.clear()
    }

    companion object {

        private val threadedIndex = object : ThreadLocal<Int>() {
            override fun initialValue(): Int {
                return 0
            }
        }

    }
}