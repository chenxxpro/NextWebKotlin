package com.github.yoojia.web

import com.github.yoojia.web.core.Engine
import javax.servlet.ServletConfig

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class BootstrapServlet : GeneralServlet() {

    override fun init2(config: ServletConfig) {
        Engine.init(config.servletContext!!)
    }
}