package com.github.yoojia.web.core

import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
internal class ConfigLoader : ConfigProvider {

    companion object {
        internal const val KEY_CONFIG_PATH = "config.path"
        internal const val KEY_CONFIG_STATE = "config.state"
    }

    /**
     * 从指定文件中加载配置信息。
     * 查看配置状态可以读取两个字段数值：
     * - config-path 配置文件路径；
     * - config-state 配置文件处理状态；
     *
     * @param path 配置文件路径
     * @return 非null Config对象。
     */
    @Suppress("UNCHECKED_CAST")
    override fun get(path: Path): Config {
        if(! Files.exists(path)) {
            val map = LinkedHashMap<String, Any>(2)
            map.put(KEY_CONFIG_PATH, path.toString())
            map.put(KEY_CONFIG_STATE, "FILE-NOT-EXISTS")
            return Config(map)
        }else{
            val map: MutableMap<String, Any>
            val stream = FileInputStream(path.toFile())
            try{
                map = Yaml().load(stream) as MutableMap<String, Any>
            }finally{
                stream.close()
            }
            map.put(KEY_CONFIG_STATE, "LOAD-SUCCESS")
            map.put(KEY_CONFIG_PATH, path.toString())
            return Config(map)
        }
    }

}