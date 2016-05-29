package com.github.yoojia.web

import com.github.yoojia.web.kernel.Context
import com.github.yoojia.web.util.splitUri
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class Request{

    val context: Context
    val raw: HttpServletRequest
    val method: String
    val path: String
    val root: String
    val processTime: Long
    val resources: List<String>

    private val mSharedParams: MutableMap<String, MutableList<String>>
    private val mDynamicParams = HashMap<String, String>()

    constructor(ctx: Context, request: HttpServletRequest) {
        context = ctx
        raw = request
        processTime = System.nanoTime()
        root = request.contextPath
        // 请求地址要去掉应用在容器中配置的ContextPath
        val uri = request.requestURI
        if ("/".equals(root)) {
            path = uri;
        }else {
            path = uri.substring(root.length);
        }
        method = request.method
        resources = splitUri(path)
        mSharedParams = HashMap<String, MutableList<String>>()
        for((key, value) in request.parameterMap) {
            mSharedParams.put(key, value.toMutableList())
        }
    }

    /**
     * 获取单个值的请求参数值。
     * - 查找请求参数，如果请求中存在多个参数值，返回第一个参数；
     * @return 字符值，如果请求中不存在此name的值则返回 null
     */
    fun param(key: String): String? {
        val values = mSharedParams[key]
        return if(values != null && values.isNotEmpty()) values.first() else null
    }

    /**
     * 获取多个值的参数值。同时查找内置参数和请求参数的值，并将它们全部以列表形式返回。
     * @return 非空值列表
     */
    fun params(key: String): List<String> {
        val dynamic = mDynamicParams[key]
        val out = ArrayList<String>()
        if(dynamic != null) {
            out.add(dynamic)
        }
        val values = mSharedParams[key]
        if(values != null && values.isNotEmpty()) {
            out.addAll(values)
        }
        return out.toList()
    }

    /**
     * 获取所有参数。
     * - 单个数值的参数以 String 返回
     * - 多个数值的参数以 List<String> 形式返回
     * @return 非空列表
     */
    fun params(): Map<String, Any> {
        val out = HashMap<String, Any>()
        for((k, v) in mSharedParams) {
            if(v.size == 1) {
                out.put(k, v.first())
            }else{
                out.put(k, v.toList())
            }
        }
        return out
    }

    /**
     * 以字符串值方式返回指定name的请求Headers值。
     * @return 字符值，如果请求中不存在此name的值则返回 null
     */
    fun header(name: String): String? {
        return raw.getHeader(name)
    }

    /**
     * 返回指定name值的Cookie。
     * @return Cookie 对象，如果请求中不存在此Cookie则返回 null
     */
    fun cookie(key: String): Cookie? {
        raw.cookies.forEach {
            if(key.equals(it.name)) {
                return it
            }
        }
        return null
    }

    /**
     * 增加多个参数对到请求中，以便在后来的请求模块中使用。
     */
    fun putParams(params: Map<String, String>) {
        for((k, v) in params) {
            putParam(k, v)
        }
    }

    /**
     * 增加一个参数对到请求中，以便在后来的请求模块中使用。
     */
    fun putParam(name: String, value: String) {
        val values = mSharedParams[name]
        if(values == null) {
            mSharedParams.put(name, mutableListOf(value))
        }else{
            values.add(value)
        }
    }

    /**
     * 获取动态参数值。
     * 注意：动态参数的有效范围是 @HttpModule 标注的模块，离开模块范围后动态参数失效。
     * @return 字符值，如果请求中不存在此name的值则返回 null
     */
    fun dynamicParam(name: String): String? {
        return mDynamicParams[name]
    }

    /**
     * 获取动态参数值，如果不存在则返回默认值。
     * 注意：动态参数的有效范围是 @HttpModule 标注的模块，离开模块范围后动态参数失效。
     * @return 字符值，如果请求中不存在此name的值则返回默认值
     */
    fun dynamicParam(name: String, defaultValue: String): String {
        val value = dynamicParam(name)
        return value?: defaultValue
    }

    fun putDynamicParams(params: Map<String, String>) {
        mDynamicParams.putAll(params)
    }

    fun clearDynamicParams(){
        mDynamicParams.clear()
    }

}