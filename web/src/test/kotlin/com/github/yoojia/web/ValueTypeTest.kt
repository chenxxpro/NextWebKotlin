package com.github.yoojia.web

import com.github.yoojia.web.util.valueType
import org.junit.Assert
import org.junit.Test

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
class ValueTypeTest {

    @Test
    fun testSuccess(){
        Assert.assertEquals(String::class.java, valueType("abc"))
        Assert.assertEquals(String::class.java, valueType("123.."))
        Assert.assertEquals(String::class.java, valueType("9."))
        Assert.assertEquals(String::class.java, valueType(".5"))
        Assert.assertEquals(String::class.java, valueType("123.a"))
        Assert.assertEquals(String::class.java, valueType("a.a"))
        Assert.assertEquals(Double::class.java, valueType("123.0"))
        Assert.assertEquals(Double::class.java, valueType("123.01"))
        Assert.assertEquals(Double::class.java, valueType("0.12"))
        Assert.assertEquals(Long::class.java, valueType("2"))
        Assert.assertEquals(Long::class.java, valueType("123456"))
    }
}