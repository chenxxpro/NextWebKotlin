package com.github.yoojia.web

import java.nio.file.Path

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.8
 */
interface ConfigProvider {

    fun getConfig(filePath: Path): Config
}