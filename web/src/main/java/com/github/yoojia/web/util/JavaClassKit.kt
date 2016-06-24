package com.github.yoojia.web.util

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
    val output = ArrayList<String>()
    Files.walkFileTree(based, object : SimpleFileVisitor<Path>() {
        @Throws(IOException::class)
        override fun visitFile(path: Path, attr: BasicFileAttributes): FileVisitResult {
            val name = based.relativize(path).toString()
            if(name.endsWith(".class")) {
                val clazz = resolveClassName(name)
                Logger.trace("-> $clazz")
                if(filter.accept(clazz)) { // return true to accept
                    output.add(clazz);
                }
            }
            return FileVisitResult.CONTINUE
        }
    })
    return output.toList()
}

fun findJarClassNames(filter: Filter<String>): List<String> {
    return emptyList() // TODO 从Jar包中加载
}

fun loadClassByName(names: List<String>): List<Class<*>> {
    val classLoader = getClassLoader()
    val out = ArrayList<Class<*>>()
    names.forEach { name ->
        out.add(loadClassByName(classLoader, name))
    }
    return out.toList()
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

fun getClassLoader(): ClassLoader {
    return Thread.currentThread().contextClassLoader
}

private fun resolveClassName(path: String): String {
    val segments = splitToArray(path, File.separatorChar, false)
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
