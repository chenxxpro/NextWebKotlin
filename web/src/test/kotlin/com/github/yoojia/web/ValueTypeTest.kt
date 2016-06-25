package com.github.yoojia.web

import com.github.yoojia.web.supports.UriValueType
import org.junit.Assert
import org.junit.Test

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
class ValueTypeTest {

    @Test
    fun testSuccess(){
        Assert.assertEquals(UriValueType.String, UriValueType.get("abc"))
        Assert.assertEquals(UriValueType.String, UriValueType.get("123.."))
        Assert.assertEquals(UriValueType.String, UriValueType.get("9."))
        Assert.assertEquals(UriValueType.String, UriValueType.get(".5"))
        Assert.assertEquals(UriValueType.String, UriValueType.get("123.a"))
        Assert.assertEquals(UriValueType.String, UriValueType.get("a.a"))
        Assert.assertEquals(UriValueType.Float, UriValueType.get("123.0"))
        Assert.assertEquals(UriValueType.Float, UriValueType.get("123.01"))
        Assert.assertEquals(UriValueType.Float, UriValueType.get("0.12"))
        Assert.assertEquals(UriValueType.Float, UriValueType.get(Float.MAX_VALUE.toString()))
        Assert.assertEquals(UriValueType.Float, UriValueType.get(Float.MIN_VALUE.toString()))
        Assert.assertEquals(UriValueType.Float, UriValueType.get(Double.MAX_VALUE.toString()))
        Assert.assertEquals(UriValueType.Float, UriValueType.get(Double.MIN_VALUE.toString()))
        Assert.assertEquals(UriValueType.Int, UriValueType.get("2"))
        Assert.assertEquals(UriValueType.Int, UriValueType.get("123456"))
        Assert.assertEquals(UriValueType.Int, UriValueType.get(Int.MAX_VALUE.toString()))
        Assert.assertEquals(UriValueType.Int, UriValueType.get(Int.MIN_VALUE.toString()))
        Assert.assertEquals(UriValueType.Int, UriValueType.get(Long.MAX_VALUE.toString()))
        Assert.assertEquals(UriValueType.Int, UriValueType.get(Long.MIN_VALUE.toString()))
    }


}