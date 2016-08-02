package com.github.yoojia.web

import com.github.yoojia.web.core.Engine
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
        return "${this.javaClass.simpleName}#${Engine.VERSION}"
    }

    override fun destroy() {
        Engine.shutdown()
    }

    override fun service(request: ServletRequest?, response: ServletResponse?) {
        Engine.process(request!!, response!!)
    }
}