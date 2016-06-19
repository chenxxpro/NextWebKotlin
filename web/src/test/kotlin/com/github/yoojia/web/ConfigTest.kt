package com.github.yoojia.web

import com.github.yoojia.web.core.Config
import org.junit.Assert
import org.junit.Test
import java.util.*

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
class ConfigTest {

    @Test
    fun testGetConfig(){
        val map = HashMap<String, Any>()
        val map1 = HashMap<String, Any>()
        val map2 = HashMap<String, Any>()
        map1.put("name", "yoojia")
        map1.put("checked", true)
        map1.put("age", 123)
        map1.put("age1", 123.0f)
        map2.put("name", "chen")
        map.put("haha", "hoho")
        map.put("key", map1)
        map.put("list", arrayListOf(map1, map2))
        val config = Config(map)
        Assert.assertEquals("yoojia", config.getConfig("key").getString("name"))
        Assert.assertEquals(true, config.getConfig("key").getBoolean("checked"))
        Assert.assertEquals(123, config.getConfig("key").getInt("age"))
        Assert.assertEquals(123.0f, config.getConfig("key").getFloat("age1"))
        Assert.assertEquals(2, config.getConfigList("list").size)
        Assert.assertEquals("chen", config.getConfigList("list")[1].getString("name"))
        
        Assert.assertEquals("hoho", config.getChecked("haha", "hehe", String::class))
    }
}