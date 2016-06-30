package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.9
 */

enum class UriValueType {

    UNDEFINED("", 0),
    STRING("string:", "string:".length),
    FLOAT("float:", "float:".length),
    DOUBLE("double:", "double:".length),
    INT("int:", "int:".length),
    LONG("long:", "long:".length);

    private constructor(prefix: kotlin.String, offset: kotlin.Int){
        this.prefix = prefix
        this.offset = offset
    }

    val prefix: kotlin.String
    val offset: kotlin.Int;

    /**
     * 当前类型与指定类型是否匹配:
     * - 当前或者指定类型为 UNDEFINED 时, 匹配;
     * - 类型相同时,匹配;
     */
    fun match(other: UriValueType): Boolean {
        if(UNDEFINED.equals(this) || UNDEFINED.equals(other)) {
            return true
        }else{
            return this.equals(other)
        }
    }

    companion object {

        private const val INT_MAX_LENGTH = Int.MAX_VALUE.toString().length
        private const val LONG_MAX_LENGTH = Long.MAX_VALUE.toString().length

        fun parse(resource: kotlin.String): UriValueType {
            val lastIndex = resource.lastIndex
            var dots = 0; var digits = 0; var marks = 0; var signs = 0
            resource.forEachIndexed { index, char ->
                if(Character.isDigit(char)) {
                    digits += 1
                }else if('E'.equals(char, ignoreCase = true)) {
                    marks += 1
                    if(0 == index || index == lastIndex) return STRING //: e123, 123E
                }else if('-'.equals(char) || '+'.equals(char)) {
                    signs += 1
                    if(index == lastIndex) return STRING //: 123-
                }else if('.'.equals(char)) {
                    dots += 1
                    if(0 == index || index == lastIndex) return STRING //: .123 , 123.
                }
                if(marks > 1 || signs > 1 || dots > 1) {
                    return STRING
                }else{
                    // Check: contains other chars: -1A.BCDEFGHAHAHA
                    val total = marks + signs + dots + digits
                    if(total < index + 1) {
                        return STRING
                    }
                }
            }
            if(marks == 0 && dots == 0) {
                val _digits = resource.length - signs
                if(digits == _digits) {
                    return when {
                        _digits <= INT_MAX_LENGTH -> INT
                        _digits <= LONG_MAX_LENGTH -> LONG
                        else -> STRING
                    }
                }else{
                    return STRING
                }
            }else{//: marks:0/1, dots:0/1
                val _digits = resource.length - signs - marks - dots
                // 根据
                return if(digits == _digits) FLOAT else STRING
            }
        }
    }
}