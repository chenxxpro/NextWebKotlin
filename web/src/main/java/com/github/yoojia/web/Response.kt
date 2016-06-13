package com.github.yoojia.web

import com.github.yoojia.web.core.Context
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class Response(val context: Context, val raw: HttpServletResponse) {

    val args = HashMap<String, Any>()

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
        raw.contentType = contextType
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
        raw.status = code
        return this
    }

    /**
     * 添加Cookie
     */
    fun addCookie(name: String, value: String): Response {
        raw.addCookie(Cookie(name, value))
        return this
    }

    /**
     * 添加Header
     */
    fun addHeader(name: String, value: String): Response {
        raw.addHeader(name, value)
        return this
    }

    /**
     * 向客户端发送服务器内部错误响应数据
     */
    fun sendError(err: Throwable): Response  {
        val text = err.message
        return sendError(text?:err.toString())
    }

    /**
     * 向客户端发送服务器内部错误响应数据
     */
    fun sendError(text: String): Response {
        raw.sendError(StatusCode.INTERNAL_SERVER_ERROR, text)
        return this
    }

    /**
     * 重定向
     */
    fun redirect(location: String): Response {
        raw.sendRedirect(location)
        return this
    }

    /**
     * 设置渲染模板
     */
    fun template(name: String): Response {
        args.put(TEMPLATE_NAME, name)
        return this
    }

    /**
     * 设置静态资源文件名
     */
    fun static(name: String): Response {
        args.put(STATIC_NAME, name)
        return this
    }

    fun putArgs(name: String, value: String): Response {
        args.put(name, value)
        return this
    }

    fun putArgs(name: String, value: Int): Response {
        args.put(name, value)
        return this
    }

    fun putArgs(name: String, value: Float): Response {
        args.put(name, value)
        return this
    }

    fun putArgs(name: String, value: Boolean): Response {
        args.put(name, value)
        return this
    }

    /**
     * 向客户端响应中写入文本数据
     */
    @Throws(Exception::class)
    fun writeTextThrows(text: String) {
        checkNotNull(text)
        raw.writer.write(text)
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
        val TEMPLATE_NAME = "next-web.response.names:template"
        val STATIC_NAME = "next-web.response.names:static"
    }
}