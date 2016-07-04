package com.github.yoojia.web

import com.github.yoojia.web.core.Engine
import javax.servlet.Servlet
import javax.servlet.ServletConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class BootstrapServlet : Servlet {

    override fun init(config: ServletConfig?) {
        Engine.boot(config?.servletContext!!)
    }

    override fun getServletConfig(): ServletConfig? {
        return null
    }

    override fun getServletInfo(): String? {
        return Engine.VERSION
    }

    override fun destroy() {
        Engine.shutdown()
    }

    override fun service(request: ServletRequest?, response: ServletResponse?) {
        Engine.process(request!!, response!!)
    }
}