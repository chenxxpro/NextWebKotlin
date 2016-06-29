package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.9
 */

enum class UriValueType {

    Any("", 0),
    String("string:", "string:".length),
    Float("float:", "float:".length),
    Int("int:", "int:".length);

    private constructor(prefix: kotlin.String, offset: kotlin.Int){
        this.prefix = prefix
        this.offset = offset
    }

    val prefix: kotlin.String
    val offset: kotlin.Int;

    /**
     * 当前类型与指定类型是否匹配:
     * - 当前或者指定类型为Any时, 匹配;
     * - 类型相同时,匹配;
     */
    fun match(other: UriValueType): Boolean {
        if(Any.equals(this) || Any.equals(other)) {
            return true
        }else{
            return this.equals(other)
        }
    }

    companion object {

        private val INT_MAX_LENGTH = Long.MAX_VALUE.toString().length

        fun parse(resource: kotlin.String): UriValueType {
            val lastIndex = resource.lastIndex
            var dots = 0; var digits = 0; var marks = 0; var signs = 0
            resource.forEachIndexed { index, char ->
                if(Character.isDigit(char)) {
                    digits += 1
                }else if('E'.equals(char, ignoreCase = true)) {
                    marks += 1
                    if(0 == index || index == lastIndex) return String //: e123, 123E
                }else if('-'.equals(char) || '+'.equals(char)) {
                    signs += 1
                    if(index == lastIndex) return String //: 123-
                }else if('.'.equals(char)) {
                    dots += 1
                    if(0 == index || index == lastIndex) return String //: .123 , 123.
                }
                if(marks > 1 || signs > 1 || dots > 1) {
                    return String
                }else{
                    // Check: contains other chars: -1A.BCDEFGHAHAHA
                    val total = marks + signs + dots + digits
                    if(total < index + 1) {
                        return String
                    }
                }
            }
            if(marks == 0 && dots == 0) {
                val _digits = resource.length - signs
                // todo check Long,Int overflow
                if(_digits > INT_MAX_LENGTH) {
                    return String
                }else {
                    return if(digits == _digits) Int else String
                }
            }else{//: marks:0/1, dots:0/1
                val _digits = resource.length - signs - marks - dots
                return if(digits == _digits) Float else String
            }
        }
    }
}