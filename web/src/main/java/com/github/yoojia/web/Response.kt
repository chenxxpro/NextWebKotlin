package com.github.yoojia.web

import com.github.yoojia.lang.DataMap
import com.github.yoojia.web.core.Context
import com.github.yoojia.web.core.Engine
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class Response(@JvmField val context: Context, @JvmField val servletResponse: HttpServletResponse) {

    @JvmField val args = DataMap(HashMap<String, Any>())
    @JvmField val createTime: Long

    init{
        createTime = System.currentTimeMillis()
        addHeader("X-Powered-By", Engine.VERSION)
    }

    /**
     * 向客户端发送文本数据
     */
    fun sendText(text: String): Response  {
        sendText(text, "text/plain; charset=utf-8")
        return this
    }

    /**
     * 向客户端发送文本数据，并指定 ContextType 类型
     */
    fun sendText(text: String, contextType: String): Response  {
        setContextType(contextType)
        return writeTextSilently(text)
    }

    /**
     * 设置ContextType
     */
    fun setContextType(contextType: String): Response {
        servletResponse.contentType = contextType
        return this
    }

    /**
     * 向客户端发送HTML数据
     */
    fun sendHtml(text: String): Response {
        sendText(text, "text/html; charset=utf-8")
        return this
    }

    /**
     * 向客户端发送JSON数据
     */
    fun sendJSON(jsonText: String): Response {
        sendText(jsonText, "application/json; charset=utf-8")
        return this
    }

    /**
     * 设置响应状态码
     */
    fun setStatusCode(code: Int): Response  {
        servletResponse.status = code
        return this
    }

    /**
     * 添加Cookie
     */
    fun addCookie(name: String, value: String): Response {
        servletResponse.addCookie(Cookie(name, value))
        return this
    }

    /**
     * 添加Header
     */
    fun addHeader(name: String, value: String): Response {
        servletResponse.addHeader(name, value)
        return this
    }

    /**
     * 向客户端发送服务器内部错误响应数据
     */
    fun sendError(err: Throwable): Response  {
        return sendError(err.message ?: err.toString())
    }

    /**
     * 向客户端发送服务器内部错误响应数据
     */
    fun sendError(text: String): Response {
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
        return putArg(TEMPLATE_NAME, name)
    }

    /**
     * 设置静态资源文件名
     */
    fun static(name: String): Response {
        return putArg(STATIC_NAME, name)
    }

    /**
     * 添加一个参数
     */
    fun putArg(name: String, value: Any): Response {
        args.put(name, value)
        return this
    }

    fun putArgs(args: Map<String, Any>): Response {
        this.args.putAll(args)
        return this
    }
    /**
     * 移除一个参数
     */
    fun removeArg(name: String) {
        args.remove(name)
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
            sendError(err)
        }finally{
            return this
        }
    }

    companion object{
        val TEMPLATE_NAME = "<next-web::response.names:template>"
        val STATIC_NAME = "<next-web::response.names:static>"
    }
}