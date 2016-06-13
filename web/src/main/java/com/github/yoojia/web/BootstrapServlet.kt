package com.github.yoojia.web

import com.github.yoojia.web.core.ClassProvider
import com.github.yoojia.web.core.ClassScanner
import com.github.yoojia.web.core.Engine
import javax.servlet.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class BootstrapServlet : Servlet {

    private val engine = Engine()

    override fun destroy() {
        engine.stop()
    }

    override fun init(config: ServletConfig?) {
        engine.start(config?.servletContext!!, ClassScanner())
    }

    fun init(context: ServletContext, classProvider: ClassProvider) {
        engine.start(context, classProvider)
    }

    override fun getServletConfig(): ServletConfig? {
        return null
    }

    override fun getServletInfo(): String? {
        return Engine.VERSION
    }

    override fun service(request: ServletRequest?, response: ServletResponse?) {
        engine.process(request!!, response!!)
    }
}