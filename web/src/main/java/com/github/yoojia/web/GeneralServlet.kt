package com.github.yoojia.web

import javax.servlet.Servlet
import javax.servlet.ServletConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.16
 */
abstract class GeneralServlet : Servlet {

    private var servletConfig: ServletConfig? = null

    final override fun init(config: ServletConfig?) {
        servletConfig = config
        init2(config!!)
    }

    abstract fun init2(config: ServletConfig)

    override fun getServletConfig(): ServletConfig? {
        return servletConfig
    }

    override fun getServletInfo(): String? {
        return "${this.javaClass.simpleName}#${Application.VERSION}"
    }

    override fun destroy() {
        Application.shutdown()
    }

    override fun service(request: ServletRequest?, response: ServletResponse?) {
        Application.service(request!!, response!!)
    }
}