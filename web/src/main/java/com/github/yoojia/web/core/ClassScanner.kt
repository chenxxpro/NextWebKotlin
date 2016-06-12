package com.github.yoojia.web.core

import com.github.yoojia.web.supports.*
import com.github.yoojia.web.util.*
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
internal class ClassScanner : ClassProvider {

    companion object{
        private val DEFAULT_SYSTEM_CLASSES = arrayOf(
                "com.github.yoojia.web.",
                "java.",
                "javax.",
                "org.xml.",
                "org.w3c."
        )
    }

    override fun get(): List<Class<*>> {
        val scanStart = now()
        // 查找所有Java Class文件
        val filter = fun(name: String): Boolean {
            DEFAULT_SYSTEM_CLASSES.forEach {
                if(name.startsWith(it)) return true
            }
            return false
        }
        val runtime = findRuntimeNames(getClassPath(), filter)
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