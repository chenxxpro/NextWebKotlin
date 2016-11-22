package com.github.yoojia.web

import com.parkingwng.lang.data.KeyMap
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class Response(@JvmField val context: Context, @JvmField val servletResponse: HttpServletResponse) {

    companion object{
        @JvmField val TEMPLATE_NAME = "nwk.response.names:template"
        @JvmField val STATIC_NAME = "nwk.response.names:static"
    }

    @JvmField val params = KeyMap(HashMap<String, Any>())

    init{
        header("X-Powered-By", Application.VERSION)
    }

    /**
     * 向客户端发送文本数据
     */
    fun text(text: String): Response  {
        text(text, "text/plain; charset=utf-8")
        return this
    }

    /**
     * 向客户端发送文本数据，并指定 ContextType 类型
     */
    fun text(text: String, contextType: String): Response  {
        contentType(contextType)
        return writeTextSilently(text)
    }

    /**
     * 向客户端发送HTML数据
     */
    fun html(text: String): Response {
        text(text, "text/html; charset=utf-8")
        return this
    }

    /**
     * 向客户端发送JSON数据
     */
    fun json(jsonText: String): Response {
        text(jsonText, "application/json; charset=utf-8")
        return this
    }

    /**
     * 设置ContextType
     */
    fun contentType(contextType: String): Response {
        servletResponse.contentType = contextType
        return this
    }

    fun contentType(): String = servletResponse.contentType

    /**
     * 设置响应状态码
     */
    fun status(code: Int): Response  {
        servletResponse.status = code
        return this
    }

    /**
     * @return
     */
    fun status(): Int = servletResponse.status

    /**
     * 添加Cookie
     */
    fun cookie(name: String, value: String): Response {
        servletResponse.addCookie(Cookie(name, value))
        return this
    }

    /**
     * 添加Header
     */
    fun header(name: String, value: String): Response {
        servletResponse.addHeader(name, value)
        return this
    }

    /**
     * 向客户端发送服务器内部错误响应数据
     */
    fun error(err: Throwable): Response  {
        return error(err.message ?: err.toString())
    }

    /**
     * 向客户端发送服务器内部错误响应数据
     */
    fun error(text: String): Response {
        servletResponse.sendError(StatusCode.INTERNAL_SERVER_ERROR, text)
        return this
    }

    /**
     * 重定向
     */
    fun redirect(location: String): Response {
        servletResponse.sendRedirect(location)
        return this
    }

    /**
     * 设置渲染模板
     */
    fun template(name: String): Response {
        return param(TEMPLATE_NAME, name)
    }

    /**
     * 设置静态资源文件名
     */
    fun static(name: String): Response {
        return param(STATIC_NAME, name)
    }

    /**
     * 添加一个参数
     */
    fun param(name: String, value: Any): Response {
        params.put(name, value)
        return this
    }

    fun params(args: Map<String, Any>): Response {
        this.params.putAll(args)
        return this
    }

    /**
     * 移除一个参数
     */
    fun removeParam(name: String) {
        params.remove(name)
    }

    /**
     * 向客户端响应中写入文本数据
     */
    @Throws(Exception::class)
    fun writeTextThrows(text: String) {
        checkNotNull(text)
        servletResponse.writer.write(text)
    }

    fun writeTextSilently(text: String): Response {
        try{
            writeTextThrows(text)
        }catch(err: Exception) {
            error(err)
        }finally{
            return this
        }
    }

}