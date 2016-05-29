package com.github.yoojia.web

import org.junit.Assert
import org.junit.Test

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.0
 */
class ReturnListFinallyTest {

    @Test
    fun test(){
        val got = returnFinallyClearCopy()
        println(got)
        Assert.assertTrue(got.isNotEmpty())
    }

    private fun returnFinallyClearCopy(): List<String>{
        val list = arrayListOf("A", "B")
        try{
            return list.toList()
        }finally{
            list.clear()
        }
    }

//    private fun returnFinallyClearRaw(): List<String>{
//        val list = arrayListOf("A", "B")
//        try{
//            return list
//        }finally{
//            list.clear()
//        }
//    }
}