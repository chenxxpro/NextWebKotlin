package com.github.yoojia.web.util

import com.github.yoojia.web.Application
import com.github.yoojia.web.supports.Filter
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

private val Logger = LoggerFactory.getLogger("JavaClass")

fun findRuntimeNames(based: Path, filter: Filter<String>): List<String> {
    val found = ArrayList<String>()
    Files.walkFileTree(based, object : SimpleFileVisitor<Path>() {
        @Throws(IOException::class)
        override fun visitFile(path: Path, attr: BasicFileAttributes): FileVisitResult {
            val classFilePath = based.relativize(path).toString()
            if(classFilePath.endsWith(".class")) {
                val className = resolveClassName(classFilePath)
                if(filter.accept(className)) { // return true to accept
                    Logger.trace("-> $className")
                    found.add(className)
                }
            }
            return FileVisitResult.CONTINUE
        }
    })
    return found.toList()
}

fun findJarClassNames(filter: Filter<String>): List<String> {
    return emptyList() // TODO 从Jar包中加载
}

fun loadClassesByNames(names: List<String>): List<Class<*>> {
    val output = ArrayList<Class<*>>()
    val classLoader = getCoreClassLoader()
    names.forEach { name ->
        output.add(loadClassByName(classLoader, name))
    }
    return output.toList()
}

fun loadClassByName(loader: ClassLoader, name: String): Class<*> {
    val output: Class<*>
    try{
        output = loader.loadClass(name)
    }catch(err: Exception) {
        throw IllegalAccessException("Fail to load: class<$name>, loader: $loader, message: ${err.message}")
    }
    return output
}

@Suppress("UNCHECKED_CAST")
fun <T> newClassInstance(clazz: Class<*>): T {
    return clazz.newInstance() as T
}

fun getCoreClassLoader(): ClassLoader {
    return Application::class.java.classLoader
}

fun tryLoadClass(className: String): Class<*>? {
    try{
        return getCoreClassLoader().loadClass(className)
    }catch(err: Throwable) {
        return null
    }
}

private fun resolveClassName(classFilePath: String): String {
    val segments = splitToArray(classFilePath, File.separatorChar, false)
    val buffer = StringBuilder()
    segments.forEach { seg ->
        if (seg.endsWith(".class")) {
            buffer.append(seg.substring(0, seg.length - 6)/* 6: .class */)
        } else {
            buffer.append(seg).append('.')
        }
    }
    return buffer.toString()
}
