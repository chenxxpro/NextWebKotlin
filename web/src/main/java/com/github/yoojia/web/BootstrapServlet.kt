package com.github.yoojia.web

import com.github.yoojia.web.kernel.ClassProvider
import com.github.yoojia.web.kernel.ClassScanner
import com.github.yoojia.web.kernel.Engine
import javax.servlet.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class BootstrapServlet : Servlet {

    private val mEngine = Engine()

    override fun destroy() {
        mEngine.stop()
    }

    override fun init(config: ServletConfig?) {
        mEngine.start(config?.servletContext!!, ClassScanner())
    }

    fun init(context: ServletContext, classProvider: ClassProvider) {
        mEngine.start(context, classProvider)
    }

    override fun getServletConfig(): ServletConfig? {
        return null
    }

    override fun getServletInfo(): String? {
        return Engine.VERSION
    }

    override fun service(request: ServletRequest?, response: ServletResponse?) {
        mEngine.process(request!!, response!!)
    }
}