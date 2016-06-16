package com.github.yoojia.web

import com.github.yoojia.web.supports.UriSegment
import org.junit.Assert
import org.junit.Test

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
class ValueTypeTest {

    @Test
    fun testSuccess(){
        Assert.assertEquals(UriSegment.ValueType.String, UriSegment.ValueType.get("abc"))
        Assert.assertEquals(UriSegment.ValueType.String, UriSegment.ValueType.get("123.."))
        Assert.assertEquals(UriSegment.ValueType.String, UriSegment.ValueType.get("9."))
        Assert.assertEquals(UriSegment.ValueType.String, UriSegment.ValueType.get(".5"))
        Assert.assertEquals(UriSegment.ValueType.String, UriSegment.ValueType.get("123.a"))
        Assert.assertEquals(UriSegment.ValueType.String, UriSegment.ValueType.get("a.a"))
        Assert.assertEquals(UriSegment.ValueType.Float, UriSegment.ValueType.get("123.0"))
        Assert.assertEquals(UriSegment.ValueType.Float, UriSegment.ValueType.get("123.01"))
        Assert.assertEquals(UriSegment.ValueType.Float, UriSegment.ValueType.get("0.12"))
        Assert.assertEquals(UriSegment.ValueType.Int, UriSegment.ValueType.get("2"))
        Assert.assertEquals(UriSegment.ValueType.Int, UriSegment.ValueType.get("123456"))
    }


}