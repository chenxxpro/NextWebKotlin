package com.github.yoojia.web

import com.github.yoojia.web.lang.splitToArray
import com.github.yoojia.web.supports.Comparator
import org.junit.Assert
import org.junit.Test

/**
 * @author Yoojia Chen (yoojia.chen@gmail.com)
 * @since 1.0
 */
class WebURITest {

    @Test
    fun testSplitURI() {
        val uri = "/users/yoojia"
        val resources = splitToArray(uri)
        Assert.assertEquals(3, resources.size)
        Assert.assertEquals("/", resources[0])
        Assert.assertEquals("yoojia", resources[2])
    }

    @Test
    fun testSplitURI_1() {
        val uri = "/users/yoojia/profile"
        val resources = splitToArray(uri)
        Assert.assertEquals(4, resources.size)
        Assert.assertEquals("/", resources[0])
        Assert.assertEquals("yoojia", resources[2])
        Assert.assertEquals("profile", resources[3])
    }

    @Test
    fun testURIMatched() {
        val req = "/users/yoojia/profile/10086"
        val define = "/users/{username}/profile/{id}"
        Assert.assertEquals(true, Comparator.createRequest("post", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("all", define)))
        Assert.assertEquals(true, Comparator.createRequest("get", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("all", define)))
        Assert.assertEquals(true, Comparator.createRequest("put", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("all", define)))
        Assert.assertEquals(true, Comparator.createRequest("delete", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("all", define)))
    }

    @Test
    fun testURIMatched0() {
        val req = "/users/yoojia/"
        val define = "/users/{username}"
        Assert.assertEquals(true, Comparator.createRequest("post", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("post", define)))
        Assert.assertEquals(false, Comparator.createRequest("get", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("post", define)))
        Assert.assertEquals(false, Comparator.createRequest("delete", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("post", define)))
        Assert.assertEquals(false, Comparator.createRequest("put", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("post", define)))
    }

    @Test
    fun testURIMatched_1() {
        val req = "/users/yoojia/profile/10086"
        val define = "/not-match/{username}/profile/{id}"
        val reqWrap = Comparator.createRequest("get", req, splitToArray(req))
        val defWrap = Comparator.createDefine("get", define)
        Assert.assertEquals(false, reqWrap.isMatchDefine(defWrap))
    }

    @Test
    fun testURIMatched_2() {
        val req = "/users/yoojia/profile/10086"
        val define = "/users/{username}/"
        Assert.assertEquals(false, Comparator.createRequest("get", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("all", define)))
    }

    @Test
    fun testURIMatched_3() {
        val req = "/users/yoojia/profile/10086"
        val define = "/users/{username}/*"
        Assert.assertEquals(true, Comparator.createRequest("get", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("all", define)))
        Assert.assertEquals(true, Comparator.createRequest("get", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("get", define)))
        Assert.assertEquals(false, Comparator.createRequest("get", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("post", define)))
    }

    @Test
    fun testURIMatched_4() {
        val req = "/"
        val define = "/admin/*"
        Assert.assertEquals(false, Comparator.createRequest("get", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("get", define)))
    }

    @Test
    fun testURIMatched_5() {
        val req = "/admin"
        val define = "/admin/permission/access/*"
        Assert.assertEquals(false, Comparator.createRequest("get", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("get", define)))
    }

    @Test
    fun testURIMatched_6() {
        val req = "/admin/10086"
        val define = "/admin/{int:user_id}"
        Assert.assertEquals(true, Comparator.createRequest("get", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("get", define)))
    }

    @Test
    fun testURIMatched_7() {
        val req = "/admin/123.456"
        val define = "/admin/{float:user_id}"
        Assert.assertEquals(true, Comparator.createRequest("get", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("get", define)))
    }

    @Test
    fun testURIMatched_8() {
        val req = "/admin/yoojia"
        val define = "/admin/{string:user_id}"
        Assert.assertEquals(true, Comparator.createRequest("get", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("get", define)))
    }

    @Test
    fun testURIMatched_9() {
        val req = "/admin/yoojia"
        val define = "/admin/{user_id}"
        Assert.assertEquals(true, Comparator.createRequest("get", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("get", define)))
    }

    @Test
    fun testURIMatched_10() {
        val req = "/admin/123456"
        val define = "/admin/{user_id}"
        Assert.assertEquals(true, Comparator.createRequest("get", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("get", define)))
    }

    @Test
    fun testURIMatched_11() {
        val req = "/admin/123456.0"
        val define = "/admin/{user_id}"
        Assert.assertEquals(true, Comparator.createRequest("get", req, splitToArray(req)).isMatchDefine(Comparator.createDefine("get", define)))
    }

    @Test
    fun testURIMatched_12() {
        val define = "/admin/{int:user_id}"
        val req1 = "/admin/123.0"
        val req2 = "/admin/abc"
        Assert.assertEquals(false, Comparator.createRequest("get", req1, splitToArray(req1)).isMatchDefine(Comparator.createDefine("get", define)))
        Assert.assertEquals(false, Comparator.createRequest("get", req2, splitToArray(req2)).isMatchDefine(Comparator.createDefine("get", define)))
    }

    @Test
    fun testURIMatched_13() {
        val define = "/admin/{float:user_id}"
        val req1 = "/admin/123"
        val req2 = "/admin/abc"
        Assert.assertEquals(false, Comparator.createRequest("get", req1, splitToArray(req1)).isMatchDefine(Comparator.createDefine("get", define)))
        Assert.assertEquals(false, Comparator.createRequest("get", req2, splitToArray(req2)).isMatchDefine(Comparator.createDefine("get", define)))
    }

    @Test
    fun testURIMatched_14() {
        val define = "/admin/{string:user_id}"
        val req1 = "/admin/123"
        val req2 = "/admin/123.456"
        Assert.assertEquals(false, Comparator.createRequest("get", req1, splitToArray(req1)).isMatchDefine(Comparator.createDefine("get", define)))
        Assert.assertEquals(false, Comparator.createRequest("get", req2, splitToArray(req2)).isMatchDefine(Comparator.createDefine("get", define)))
    }

}