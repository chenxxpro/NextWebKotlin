package com.github.yoojia.web.kernel

import com.github.yoojia.web.supports.Logger
import com.github.yoojia.web.util.escape
import com.github.yoojia.web.util.loadConfig
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import javax.servlet.ServletContext

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class Context(val servletContext: ServletContext) {

    val config: Config
    val webPath: String


    init{
        webPath = servletContext.getRealPath("/")
        val start = System.currentTimeMillis()
        val path = resolvePath("WEB-INF${File.separator}next.yml")
        config = loadConfig(path)
        Logger.d("Config-File: $path")
        Logger.d("Config-Load-Time: ${escape(start)}ms")
    }

    /**
     * 解析路径
     */
    fun resolvePath(path: Path): Path {
        return resolvePath(path.toString())
    }

    /**
     * 解析路径
     */
    fun resolvePath(path: String): Path {
        return Paths.get(webPath, path)
    }

}