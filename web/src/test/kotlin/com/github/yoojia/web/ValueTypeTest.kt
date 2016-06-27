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
        Assert.assertEquals(UriValueType.String, UriValueType.parse("-A11"))
        Assert.assertEquals(UriValueType.String, UriValueType.parse("abc"))
        Assert.assertEquals(UriValueType.String, UriValueType.parse("123e"))
        Assert.assertEquals(UriValueType.String, UriValueType.parse("123ea"))
        Assert.assertEquals(UriValueType.String, UriValueType.parse("123E"))
        Assert.assertEquals(UriValueType.String, UriValueType.parse("A123E"))
        Assert.assertEquals(UriValueType.String, UriValueType.parse("E12A"))
        Assert.assertEquals(UriValueType.String, UriValueType.parse("123.."))
        Assert.assertEquals(UriValueType.String, UriValueType.parse("9."))
        Assert.assertEquals(UriValueType.String, UriValueType.parse(".5"))
        Assert.assertEquals(UriValueType.String, UriValueType.parse("123.a"))
        Assert.assertEquals(UriValueType.String, UriValueType.parse("a.a"))
        Assert.assertEquals(UriValueType.Float, UriValueType.parse("123.0"))
        Assert.assertEquals(UriValueType.Float, UriValueType.parse("123.01"))
        Assert.assertEquals(UriValueType.Float, UriValueType.parse("0.12"))
        Assert.assertEquals(UriValueType.Float, UriValueType.parse(Float.MAX_VALUE.toString()))
        Assert.assertEquals(UriValueType.Float, UriValueType.parse(Float.MIN_VALUE.toString()))
        Assert.assertEquals(UriValueType.Float, UriValueType.parse(Double.MAX_VALUE.toString()))
        Assert.assertEquals(UriValueType.Float, UriValueType.parse(Double.MIN_VALUE.toString()))
        Assert.assertEquals(UriValueType.Int, UriValueType.parse("2"))
        Assert.assertEquals(UriValueType.Int, UriValueType.parse("+12"))
        Assert.assertEquals(UriValueType.Int, UriValueType.parse("-12"))
        Assert.assertEquals(UriValueType.Int, UriValueType.parse("123456"))
        Assert.assertEquals(UriValueType.Int, UriValueType.parse(Int.MAX_VALUE.toString()))
        Assert.assertEquals(UriValueType.Int, UriValueType.parse(Int.MIN_VALUE.toString()))
        Assert.assertEquals(UriValueType.Int, UriValueType.parse(Long.MAX_VALUE.toString()))
        Assert.assertEquals(UriValueType.String, UriValueType.parse("${Long.MAX_VALUE.toString()}9"))
        Assert.assertEquals(UriValueType.Int, UriValueType.parse(Long.MIN_VALUE.toString()))
        Assert.assertEquals(UriValueType.String, UriValueType.parse("${Long.MIN_VALUE.toString()}9"))
    }


}