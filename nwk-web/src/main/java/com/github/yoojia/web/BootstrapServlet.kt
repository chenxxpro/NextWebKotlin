package com.github.yoojia.web

import javax.servlet.ServletConfig

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class BootstrapServlet : GeneralServlet() {

    override fun init2(config: ServletConfig) {
        Application.setup(config.servletContext!!)
    }
}