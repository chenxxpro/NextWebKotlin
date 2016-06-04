package com.github.yoojia.web.kernel

import com.github.yoojia.web.supports.*
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
internal class ClassScanner : ClassProvider {

    override fun get(): List<Class<*>> {
        val scanStart = now()
        // 查找所有Java Class文件
        val kernelPath = Paths.get(listOf("com","github","yoojia","web").joinToString(File.separator))
        val filter = fun(path: Path): Boolean {
            return ! path.startsWith(kernelPath)
        }
        val runtime = findRuntimeNames(based = getClassPath(), filter = filter)
        val jar = findJarClassNames(filter)
        val classes = ArrayList<Class<*>>(loadClassByName(runtime.concat(jar)))
        Logger.d("Classes-Scan: ${escape(scanStart)}ms")
        Logger.d("Classes-Count: ${classes.size}")
        return classes
    }

    private fun getClassPath(): Path {
        return Paths.get(Engine::class.java.getResource("/").toURI())
    }

}