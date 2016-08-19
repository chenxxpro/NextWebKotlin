package com.github.yoojia.web

import com.github.yoojia.lang.DataMap
import com.github.yoojia.lang.StreamKit
import com.github.yoojia.web.core.Context
import com.github.yoojia.web.supports.Comparator
import com.github.yoojia.web.util.splitToArray
import java.io.InputStreamReader
import java.io.StringWriter
import java.net.URLDecoder
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class Request(ctx: Context, request: HttpServletRequest){

    @JvmField val servletRequest: HttpServletRequest
    @JvmField val context: Context
    @JvmField val method: String
    @JvmField val path: String
    @JvmField val contextPath: String
    @JvmField val createTime: Long

    val resources: List<String>
    val comparator: Comparator

    private val dynamicParams = DataMap(HashMap<String, Any>())
    private val scopeParams: MutableMap<String, MutableList<String>> by lazy {
        val params: MutableMap<String, MutableList<String>> = HashMap()
        for((key, value) in request.parameterMap) {
            params.put(key, value.toMutableList())
        }
        if(request.method.toUpperCase() in PUT_DELETE) {
            readBodyStream()?.let { data ->
                params.put(BODY_DATA_NAME, mutableListOf(data))
                if(CONTENT_TYPE_FORM.equals(request.contentType, ignoreCase = true)) {
                    data.split('&').forEach { pair ->
                        val kv = pair.split('=')
                        if(kv.size != 2) throw IllegalArgumentException("Client request post invalid query string")
                        putOrNew(kv[0], URLDecoder.decode(kv[1], "UTF-8"), params)
                    }
                }
            }
        }
        /* return */params
    }

    companion object {
        private val PUT_DELETE = setOf("PUT", "DELETE")
        private val GET_POST = setOf("GET", "POST")

        @JvmField val CONTENT_TYPE_FORM = "application/x-www-form-urlencoded"
        @JvmField val CONTENT_TYPE_MULTIPART = "multipart/form-data"
        @JvmField val BODY_DATA_NAME = "<next-web::body.data:raw-data:key>"
    }

    init {
        createTime = System.currentTimeMillis()
        context = ctx
        servletRequest = request
        contextPath = request.contextPath
        val uri = request.requestURI
        path = if ("/".equals(contextPath)) uri else uri.substring(contextPath.length)
        method = request.method.toUpperCase()
        resources = splitToArray(path)
        comparator = Comparator.createRequest(method, path, resources)
    }

    /**
     * 读取BodyData (InputStream) 的文本数据。
     * 允许重复读取。第一次读取BodyData后，Request会将数据缓存到 scopeParams.BODY_DATA_NAME 中。
     * HTTP 的各个方法的数据读取逻辑：
     * - GET/POST 在调用bodyData()时检查；
     * - PUT/DELETE 在调用任意params相关接口时才检查和加载（LazyLoad）
     * @return 文本数据。如果不存在数据则返回 null
     */
    fun bodyData(): String? {
        val cached = scopeParams[BODY_DATA_NAME]?.firstOrNull()
        if(cached == null && method in GET_POST) {
            val data = readBodyStream()
            if(data != null) {
                scopeParams.put(BODY_DATA_NAME, mutableListOf(data))
            }else{
                scopeParams.put(BODY_DATA_NAME, mutableListOf(/*empty*/))
            }
            return data
        }else{
            return cached
        }
    }

    @Deprecated(message = "Use paramOrNull(String) instead",replaceWith = ReplaceWith("paramOrNull", "String"))
    fun param(name: String): String? {
        return paramOrNull(name)
    }

    /**
     * 获取指定参数名的值。
     * - 请求中存在单个值，返回值本身；
     * - 请求中存在多个值，返回第一个值；
     * @return 参数值。如果请求中不存在此参数，返回 null
     */
    fun paramOrNull(name: String): String? {
        scopeParams[name]?.let { values ->
            return values.firstOrNull()
        }
        return null
    }

    /**
     * 获取所有参数。
     * - 单个数值的参数以 String 类型返回
     * - 多个数值的参数以 List<String> 类型返回
     * @return 非空AnyMap对象
     */
    fun params(): DataMap {
        val map = DataMap(HashMap<String, Any>())
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
     * 获取所有Header
     * - 单个数值的参数以 String 类型返回
     * - 多个数值的参数以 List<String> 类型返回
     * @return 非空AnyMap对象
     */
    fun headers(): DataMap {
        val map = DataMap(HashMap<String, Any>())
        servletRequest.headerNames?.let { headerNames->
            for (name in headerNames) {
                servletRequest.getHeaders(name)?.let { headers->
                    val list = headers.toList()
                    if(list.size == 1) {
                        map.put(name, list.first())
                    }else{
                        map.put(name, list)
                    }
                }
            }
        }
        return map
    }

    /**
     * 返回指定name值的Cookie。
     * @return Cookie 对象，如果请求中不存在此Cookie则返回 null
     */
    fun cookie(name: String): Cookie? {
        servletRequest.cookies?.let { cookies->
            cookies.forEach { cookie ->
                if(name.equals(cookie.name)) {
                    return cookie
                }
            }
        }
        return null
    }

    fun cookies(): List<Cookie> {
        val cookies = servletRequest.cookies
        return if(cookies != null) cookies.toList() else emptyList()
    }

    /**
     * 增加多个参数对到请求中
     */
    fun putParam(params: Map<String, String>) {
        for((k, v) in params) {
            putParam(k, v)
        }
    }

    /**
     * 增加一个参数对到请求中
     */
    fun putParam(name: String, value: String) {
        putOrNew(name, value, scopeParams)
    }

    /**
     * 增加一个参数对到请求中
     */
    fun putParam(name: String, value: Any) {
        putParam(name, value.toString())
    }

    /**
     * 移除一个参数
     */
    fun removeParam(name: String) {
        scopeParams.remove(name)
    }

    // dynamic params

    fun dynamicParam(name: String, defaultValue: String): String {
        val value = dynamicParams[name] as String?
        if(value != null) {
            return value
        }else{
            return defaultValue
        }
    }

    fun dynamicParam(name: String): String {
        return dynamicParam(name, "")
    }

    fun dynamicIntParam(name: String): Int {
        return dynamicParam(name, "0").toInt()
    }

    fun dynamicLongParam(name: String): Long {
        return dynamicParam(name, "0").toLong()
    }

    fun dynamicFloatParam(name: String): Float {
        return dynamicParam(name, "0").toFloat()
    }

    fun dynamicDoubleParam(name: String): Double {
        return dynamicParam(name, "0").toDouble()
    }

    fun dynamicBooleanParam(name: String): Boolean {
        return dynamicParam(name, "false").toBoolean()
    }

    // extensions

    fun stringParam(name: String, def: String): String {
        val value = paramOrNull(name)
        if(value.isNullOrEmpty()) {
            return def
        }else{
            return value!!
        }
    }

    fun stringParam(name: String): String {
        return stringParam(name, "")
    }

    fun intParam(name: String, def: Int): Int {
        val value = paramOrNull(name)
        if(value.isNullOrEmpty()) {
            return def
        }else{
            return value!!.toInt()
        }
    }

    fun intParam(name: String): Int {
        return intParam(name, 0)
    }

    fun longParam(name: String, def: Long): Long {
        val value = paramOrNull(name)
        if(value.isNullOrEmpty()) {
            return def
        }else{
            return value!!.toLong()
        }
    }

    fun longParam(name: String): Long {
        return longParam(name, 0L)
    }

    fun floatParam(name: String, def: Float): Float {
        val value = paramOrNull(name)
        if(value.isNullOrEmpty()) {
            return def
        }else{
            return value!!.toFloat()
        }
    }

    fun floatParam(name: String): Float {
        return floatParam(name, 0f)
    }

    fun doubleParam(name: String, def: Double): Double {
        val value = paramOrNull(name)
        if(value.isNullOrEmpty()) {
            return def
        }else{
            return value!!.toDouble()
        }
    }

    fun doubleParam(name: String): Double {
        return doubleParam(name, 0.0)
    }

    fun booleanParam(name: String, def: Boolean): Boolean {
        val value = paramOrNull(name)
        if(value.isNullOrEmpty()) {
            return def
        }else{
            return value!!.toBoolean()
        }
    }

    fun booleanParam(name: String): Boolean {
        return booleanParam(name, false)
    }

    /// framework methods

    internal fun putDynamicScopeParams(params: Map<String, String>) {
        dynamicParams.putAll(params)
    }

    internal fun removeDynamicScopeParams(){
        dynamicParams.clear()
    }

    //// supports

    private fun readBodyStream(): String? {
        val output = StringWriter()
        val input = InputStreamReader(servletRequest.inputStream)
        val count = StreamKit.copy(input, output)
        return if(count > 0) output.toString() else null
    }

    private fun putOrNew(name: String, value: String, map: MutableMap<String, MutableList<String>>) {
        val values = map[name]
        if(values != null) {
            values.add(value)
        }else{
            map.put(name, mutableListOf(value))
        }
    }
}