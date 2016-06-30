package com.github.yoojia.web

import com.github.yoojia.web.supports.UriValueType
import com.github.yoojia.web.supports.createDefineUriSegment
import com.github.yoojia.web.supports.createRequestUriSegment
import org.junit.Assert
import org.junit.Test

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
class UriSegmentTest {

    @Test
    fun testParseSuccess(){
        createDefineUriSegment("abc")
        createRequestUriSegment("abc")
        createDefineUriSegment("{abc}")
        createDefineUriSegment("{string:abc}")
        createDefineUriSegment("{int:abc}")
        createDefineUriSegment("{float:abc}")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseFail1(){
        createDefineUriSegment("{abc")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseFail2(){
        createDefineUriSegment("abc}")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseFail3(){
        createDefineUriSegment("string:abc}")
    }

    @Test
    fun test(){
        Assert.assertEquals(UriValueType.INT, createRequestUriSegment("123").valueType)
        Assert.assertEquals(UriValueType.INT, createRequestUriSegment("-123").valueType)
        Assert.assertEquals(UriValueType.FLOAT, createRequestUriSegment("123.0").valueType)
        Assert.assertEquals(UriValueType.FLOAT, createRequestUriSegment("-123.0").valueType)
        Assert.assertEquals(UriValueType.INT, createRequestUriSegment("+12").valueType)
        Assert.assertEquals(UriValueType.FLOAT, createRequestUriSegment("+123.0").valueType)
        Assert.assertEquals(UriValueType.STRING, createRequestUriSegment("123abc").valueType)
        Assert.assertEquals(UriValueType.STRING, createRequestUriSegment("/").valueType)
        Assert.assertEquals(UriValueType.STRING, createRequestUriSegment("*").valueType)
    }

    @Test
    fun testDynamic(){
        Assert.assertEquals(UriValueType.INT, createDefineUriSegment("{int:id}").valueType)
        Assert.assertEquals(UriValueType.FLOAT, createDefineUriSegment("{float:id}").valueType)
        Assert.assertEquals(UriValueType.STRING, createDefineUriSegment("{string:id}").valueType)
        Assert.assertEquals(UriValueType.UNDEFINED, createDefineUriSegment("{id}").valueType)
    }

    @Test
    fun testResourceName(){
        val id = createDefineUriSegment("id")
        Assert.assertEquals("id", id.segment)

        val user = createDefineUriSegment("{user}")
        Assert.assertEquals("user", user.segment)

        val pass = createDefineUriSegment("{string:pass}")
        Assert.assertEquals("pass", pass.segment)

        val age = createDefineUriSegment("{int:age}")
        Assert.assertEquals("age", age.segment)

        val weight = createDefineUriSegment("{float:weight}")
        Assert.assertEquals("weight", weight.segment)
    }
}