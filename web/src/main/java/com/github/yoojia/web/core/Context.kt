package com.github.yoojia.web.core

import java.nio.file.Path
import java.nio.file.Paths
import javax.servlet.ServletContext

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class Context(val webPath: String, val config: Config, val servletContext: ServletContext) {

    val contextPath: String by lazy {
        val path = servletContext.contextPath
        if(path.isNullOrEmpty()) "/" else path
    }

    fun resolvePath(path: Path): Path {
        return resolvePath(path.toString())
    }

    fun resolvePath(path: String): Path {
        return Paths.get(webPath, path)
    }

}