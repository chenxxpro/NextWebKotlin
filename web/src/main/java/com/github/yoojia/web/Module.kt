package com.github.yoojia.web

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
interface Module : Plugin {

    /**
     * 初始化模块。
     */
    fun prepare(inputs: List<Class<*>>): List<Class<*>> = emptyList()

    /**
     * 处理请求
     */
    fun process(request: Request, response: Response, router: Router)

}