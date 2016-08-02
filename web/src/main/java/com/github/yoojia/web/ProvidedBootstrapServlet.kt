package com.github.yoojia.web

import com.github.yoojia.web.core.ClassProvider
import com.github.yoojia.web.core.Config
import com.github.yoojia.web.core.ConfigProvider
import com.github.yoojia.web.core.Engine
import java.nio.file.Path
import javax.servlet.ServletConfig

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.5
 */
abstract class ProvidedBootstrapServlet : GeneralServlet(), ClassProvider, ConfigProvider {

    override fun init2(config: ServletConfig) {
        Engine.start(config.servletContext!!, this, this)
    }

    override fun get(filePath: Path): Config {
        return Config.empty()
    }

    companion object {
        @JvmStatic fun from(vararg classes: Class<*>) : List<Class<*>> {
            return classes.toList()
        }
    }
}