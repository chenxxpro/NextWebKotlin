package com.github.yoojia.web

import com.github.yoojia.web.core.ClassProvider
import com.github.yoojia.web.core.Config
import com.github.yoojia.web.core.ConfigProvider
import com.github.yoojia.web.core.Engine
import java.nio.file.Path
import javax.servlet.Servlet
import javax.servlet.ServletConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.5
 */
abstract class ProvidedBootstrapServlet : Servlet, ClassProvider, ConfigProvider {

    override fun init(config: ServletConfig?) {
        Engine.boot(config?.servletContext!!, this, this)
    }

    override fun getServletConfig(): ServletConfig? {
        return null
    }

    override fun getServletInfo(): String? {
        return Engine.VERSION
    }

    override fun service(request: ServletRequest?, response: ServletResponse?) {
        Engine.process(request!!, response!!)
    }

    override fun get(path: Path): Config {
        return Config.empty()
    }

    override fun destroy() {
        Engine.shutdown()
    }

    companion object {

        @JvmStatic fun from(vararg classes: Class<*>) : List<Class<*>> {
            return classes.toList()
        }
    }
}