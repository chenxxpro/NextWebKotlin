package com.github.yoojia.web

import com.github.yoojia.web.supports.UriSegment
import com.github.yoojia.web.supports.UriValueType
import org.junit.Assert
import org.junit.Test

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 1.0
 */
class UriSegmentTest {

    @Test
    fun testParseSuccess(){
        UriSegment.fromDefine("abc")
        UriSegment.fromRequest("abc")
        UriSegment.fromDefine("{abc}")
        UriSegment.fromDefine("{string:abc}")
        UriSegment.fromDefine("{int:abc}")
        UriSegment.fromDefine("{float:abc}")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseFail1(){
        UriSegment.fromDefine("{abc")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseFail2(){
        UriSegment.fromDefine("abc}")
    }

    @Test(expected = IllegalArgumentException::class)
    fun testParseFail3(){
        UriSegment.fromDefine("string:abc}")
    }

    @Test
    fun test(){
        Assert.assertEquals(UriValueType.Int, UriSegment.fromRequest("123").type)
        Assert.assertEquals(UriValueType.Int, UriSegment.fromRequest("-123").type)
        Assert.assertEquals(UriValueType.Float, UriSegment.fromRequest("123.0").type)
        Assert.assertEquals(UriValueType.Float, UriSegment.fromRequest("-123.0").type)
        Assert.assertEquals(UriValueType.String, UriSegment.fromRequest("+12").type)
        Assert.assertEquals(UriValueType.String, UriSegment.fromRequest("+123.0").type)
        Assert.assertEquals(UriValueType.String, UriSegment.fromRequest("123abc").type)
        Assert.assertEquals(UriValueType.String, UriSegment.fromRequest("/").type)
        Assert.assertEquals(UriValueType.String, UriSegment.fromRequest("*").type)
    }

    @Test
    fun testDynamic(){
        Assert.assertEquals(UriValueType.Int, UriSegment.fromDefine("{int:id}").type)
        Assert.assertEquals(UriValueType.Float, UriSegment.fromDefine("{float:id}").type)
        Assert.assertEquals(UriValueType.String, UriSegment.fromDefine("{string:id}").type)
        Assert.assertEquals(UriValueType.Any, UriSegment.fromDefine("{id}").type)
    }

    @Test
    fun testResourceName(){
        val id = UriSegment.fromDefine("id")
        Assert.assertEquals("id", id.segment)

        val user = UriSegment.fromDefine("{user}")
        Assert.assertEquals("user", user.segment)

        val pass = UriSegment.fromDefine("{string:pass}")
        Assert.assertEquals("pass", pass.segment)

        val age = UriSegment.fromDefine("{int:age}")
        Assert.assertEquals("age", age.segment)

        val weight = UriSegment.fromDefine("{float:weight}")
        Assert.assertEquals("weight", weight.segment)
    }
}