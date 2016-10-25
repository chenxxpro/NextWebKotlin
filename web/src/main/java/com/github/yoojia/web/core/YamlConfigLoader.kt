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
internal class YamlConfigLoader : ConfigProvider {

    companion object {
        internal const val KEY_CONFIG_PATH = "nwk.core.config.path"
        internal const val KEY_CONFIG_STATE = "nwk.core.config.state"
    }

    /**
     * 从指定文件中加载配置信息。
     * 查看配置状态可以读取两个字段数值：
     * - config-path 配置文件路径；
     * - config-state 配置文件处理状态；
     *
     * @param filePath 配置文件路径
     * @return 非null Config对象。
     */
    @Suppress("UNCHECKED_CAST")
    override fun getConfig(filePath: Path): Config {
        if(! Files.exists(filePath)) {
            val map = LinkedHashMap<String, Any>(2)
            map.put(KEY_CONFIG_PATH, filePath.toString())
            map.put(KEY_CONFIG_STATE, "FILE-NOT-EXISTS")
            return Config(map)
        }else{
            val map: MutableMap<String, Any>
            val stream = FileInputStream(filePath.toFile())
            try{
                map = Yaml().load(stream) as MutableMap<String, Any>
            }finally{
                stream.close()
            }
            map.put(KEY_CONFIG_STATE, "LOAD-SUCCESS")
            map.put(KEY_CONFIG_PATH, filePath.toString())
            return Config(map)
        }
    }

}