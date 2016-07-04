package com.github.yoojia.web.core

import com.github.yoojia.web.Request
import com.github.yoojia.web.Response

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
interface Module : Plugin {

    /**
     * 初始化模块。
     */
    fun prepare(inputs: List<Class<*>>): List<Class<*>>{
        return emptyList()
    }

    /**
     * 处理请求
     */
    fun process(request: Request, response: Response, dispatch: DispatchChain) {
        dispatch.next(request, response, dispatch)
    }

}