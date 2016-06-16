package com.github.yoojia.web

import com.github.yoojia.web.supports.UriSegment
import org.junit.Assert
import org.junit.Test

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
class UriSegmentTest {

    @Test
    fun testParseSuccess(){
        UriSegment("abc")
        UriSegment("{abc}")
        UriSegment("{string:abc}")
        UriSegment("{int:abc}")
        UriSegment("{float:abc}")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseFail1(){
        UriSegment("{abc")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseFail2(){
        UriSegment("abc}")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseFail3(){
        UriSegment("string:abc}")
    }

    @Test
    fun test(){
        Assert.assertEquals(UriSegment.ValueType.Int, UriSegment("123").type)
        Assert.assertEquals(UriSegment.ValueType.Int, UriSegment("123").type)
        Assert.assertEquals(UriSegment.ValueType.Float, UriSegment("123.0").type)
        Assert.assertEquals(UriSegment.ValueType.String, UriSegment("123abc").type)
        Assert.assertEquals(UriSegment.ValueType.String, UriSegment("/").type)
        Assert.assertEquals(UriSegment.ValueType.String, UriSegment("*").type)
    }

    @Test
    fun testDynamic(){
        Assert.assertEquals(UriSegment.ValueType.Int, UriSegment("{int:id}").type)
        Assert.assertEquals(UriSegment.ValueType.Float, UriSegment("{float:id}").type)
        Assert.assertEquals(UriSegment.ValueType.String, UriSegment("{string:id}").type)
        Assert.assertEquals(UriSegment.ValueType.Any, UriSegment("{id}").type)
    }
}