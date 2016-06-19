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
        map.put("key", map1)
        map.put("list", arrayListOf(map1, map2))
        Assert.assertEquals("yoojia", Config(map).getConfig("key").getString("name"))
        Assert.assertEquals(true, Config(map).getConfig("key").getBoolean("checked"))
        Assert.assertEquals(123, Config(map).getConfig("key").getInt("age"))
        Assert.assertEquals(123.0f, Config(map).getConfig("key").getFloat("age1"))
        Assert.assertEquals(2, Config(map).getConfigList("list").size)
        Assert.assertEquals("chen", Config(map).getConfigList("list")[1].getString("name"))
    }
}