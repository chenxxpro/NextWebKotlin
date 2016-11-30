package com.github.yoojia.web.supports

import com.github.yoojia.web.Request
import com.github.yoojia.web.Response
import java.lang.reflect.Method

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.3
 */
interface ModuleRequestsListener : ModuleListener{

    /**
     * 在模块内每个请求执行前回调
     */
    fun beforeRequests(method: Method, request: Request, response: Response)

    /**
     * 在模块内每个请求执行后回调
     */
    fun afterRequests(method: Method, request: Request, response: Response)
}