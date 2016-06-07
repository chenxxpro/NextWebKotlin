package com.github.yoojia.web.kernel

import java.nio.file.Path
import java.nio.file.Paths
import javax.servlet.ServletContext

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class Context(val webPath: String, val config: Config, val servletContext: ServletContext) {

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