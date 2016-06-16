package com.github.yoojia.web

import com.github.yoojia.web.core.Context
import com.github.yoojia.web.util.AnyMap
import com.github.yoojia.web.util.splitToArray
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class Request{

    // Servlet
    val servletRequest: HttpServletRequest
    val context: Context

    // HTTP method
    val method: String

    // Request path
    val path: String

    // Request context path
    val contextPath: String

    // Request create time
    val createTime: Long

    // Path to array
    val resources: List<String>

    // 整个请求范围内生效的参数：请求参数
    private val scopeParams: MutableMap<String, MutableList<String>>

    // 仅限于处理方法范围内有效的参数：动态参数
    private val dynamicParams = AnyMap()

    constructor(ctx: Context, request: HttpServletRequest) {
        createTime = System.nanoTime()
        context = ctx
        servletRequest = request
        contextPath = request.contextPath
        // 请求地址要去掉应用在容器中配置的ContextPath
        val uri = request.requestURI
        path = if ("/".equals(contextPath)) uri else uri.substring(contextPath.length)
        method = request.method
        resources = splitToArray(path)

        scopeParams = HashMap<String, MutableList<String>>()
        for((key, value) in request.parameterMap) {
            scopeParams.put(key, value.toMutableList())
        }
    }

    /**
     * 获取单个值的请求参数值。
     * - 查找请求参数，如果请求中存在多个参数值，返回第一个参数；
     * @return 字符值，如果请求中不存在此name的值则返回 null
     */
    fun param(key: String): String? {
        val values = scopeParams[key]
        return if(values != null && values.isNotEmpty()) values.first() else null
    }

    /**
     * 获取所有参数。
     * - 单个数值的参数以 String 返回
     * - 多个数值的参数以 List<String> 形式返回
     * @return 非空AnyMap对象
     */
    fun params(): AnyMap {
        val map = AnyMap()
        for((k, v) in scopeParams) {
            if(v.size == 1) {
                map.put(k, v.first())
            }else{
                map.put(k, v.toList())
            }
        }
        return map
    }

    /**
     * 以字符串值方式返回指定name的请求Headers值。
     * @return 字符值，如果请求中不存在此name的值则返回 null
     */
    fun header(name: String): String? {
        return servletRequest.getHeader(name)
    }

    /**
     * 返回指定name值的Cookie。
     * @return Cookie 对象，如果请求中不存在此Cookie则返回 null
     */
    fun cookie(key: String): Cookie? {
        servletRequest.cookies.forEach { cookie ->
            if(key.equals(cookie.name)) {
                return cookie
            }
        }
        return null
    }

    /**
     * 增加多个参数对到请求中，以便在后来的请求模块中使用。
     */
    fun putParam(params: Map<String, String>) {
        for((k, v) in params) {
            putParam(k, v)
        }
    }

    /**
     * 增加一个参数对到请求中，以便在后来的请求模块中使用。
     */
    fun putParam(name: String, value: String) {
        val values = scopeParams[name]
        if(values != null) {
            values.add(value)
        }else{
            scopeParams.put(name, mutableListOf(value))
        }
    }

    /**
     * 获取动态参数值，如果不存在则返回默认值。
     * 注意：动态参数的有效范围是 @GET/POST/PUT/DELETE 标注的模块，离开模块范围后动态参数失效。
     * @return 字符值，如果请求中不存在此name的值则返回默认值
     */
    fun dynamicParam(name: String, defaultValue: String? = null): String? {
        val value = dynamicParams[name] as String?
        if(value != null) {
            return value
        }else{
            return defaultValue
        }
    }

    fun dynamicParam(name: String): String? {
        return dynamicParam(name, null)
    }

    /// framework methods

    fun _setDynamicScope(params: Map<String, String>) {
        dynamicParams.putAll(params)
    }

    fun _resetDynamicScope(){
        dynamicParams.clear()
    }

}