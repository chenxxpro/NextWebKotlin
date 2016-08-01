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

    val prefix: String
    val offset: Int

    private constructor(prefix: String, offset: Int){
        this.prefix = prefix
        this.offset = offset
    }

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

        fun parse(resource: kotlin.String): UriValueType {
            val lastIndex = resource.lastIndex
            var dots = 0; var digits = 0; var marks = 0; var signs = 0
            var markIndex = -1
            resource.forEachIndexed { index, char ->
                if(Character.isDigit(char)) {
                    digits += 1
                }else if('E'.equals(char, ignoreCase = true)) {
                    if(0 == index || index == lastIndex) return STRING //: e123, 123E
                    marks += 1
                    markIndex = index
                }else if('-'.equals(char)) {
                    if(index == lastIndex) return STRING //: 123-
                    if(index != 0 && index == (markIndex + 1)) {
                        marks += 1 // 1.4E-23
                    }else{
                        signs += 1
                    }
                }else if('+'.equals(char)) {
                    if(index == lastIndex) return STRING //: 123+
                    signs += 1
                }else if('.'.equals(char)) {
                    if(0 == index || index == lastIndex) return STRING //: .123 , 123.
                    dots += 1
                }
                if(marks/*[E, -]*/ > 2 || signs > 1 || dots > 1) {
                    return STRING
                }else{ // Check: contains other chars
                    val total = marks + signs + dots + digits
                    if(total < index + 1) {
                        return STRING
                    }
                }
            }
            if(marks == 0 && dots == 0) {
                val _digits = resource.length - signs
                if(digits == _digits) {
                    val value: Long
                    try{
                        value = resource.toLong()
                    }catch(err: NumberFormatException) {
                        return STRING
                    }
                    if(value in Int.MIN_VALUE..Int.MAX_VALUE) {
                        return INT
                    }else{
                        return LONG
                    }
                }else{
                    return STRING
                }
            }else{//: marks:0/1, dots:0/1
                val _digits = resource.length - (signs + marks + dots)
                if(digits == _digits) {
                    val value: Double
                    try{
                        value = resource.toDouble()
                    }catch(err: NumberFormatException) {
                        return STRING
                    }
                    if(Math.abs(value.toFloat()) in Float.MIN_VALUE..Float.MAX_VALUE) {
                        return FLOAT
                    }else{
                        return DOUBLE
                    }
                }else{
                    return STRING
                }
            }
        }
    }
}