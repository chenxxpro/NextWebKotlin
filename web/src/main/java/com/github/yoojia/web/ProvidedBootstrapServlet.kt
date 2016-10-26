package com.github.yoojia.web

import java.nio.file.Path
import javax.servlet.ServletConfig

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.5
 */
abstract class ProvidedBootstrapServlet : GeneralServlet(), ClassProvider, ConfigProvider {

    override fun init2(config: ServletConfig) {
        AppEngine.startup(config.servletContext!!, this, this)
    }

    override fun getConfig(filePath: Path): Config {
        return Config.empty()
    }

    companion object {
        @JvmStatic fun from(vararg classes: Class<*>) : List<Class<*>> {
            return classes.toList()
        }
    }
}