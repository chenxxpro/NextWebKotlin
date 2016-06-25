package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 0.1
 */

enum class UriValueType {

    Any,
    String,
    Float,
    Int;

    fun match(other: UriValueType): Boolean {
        if(Any.equals(this) || Any.equals(other)) {
            return true
        }else{
            return this.equals(other)
        }
    }

    companion object {

        // resource is a shot string !!!
        // Double: float, double is digits and '.'
        // Long: int, long is all digits
        // String: string, otherwise
        fun get(resource: kotlin.String): UriValueType {
            var dots = 0
            var digits = 0
            resource.forEachIndexed { i, char ->
                if('.'.equals(char)) {
                    dots += 1
                    if(dots > 1 /* 12..6 */|| i == 0/* .5 */ || i == (resource.length - 1)/* 124. */) {
                        dots = -1
                        return@forEachIndexed
                    }
                }else if(Character.isDigit(char) || (i == 0 && '-'.equals(char))) {
                    digits += 1
                }
            }
            when{
                dots == 0 && digits == resource.length -> return Int
                dots == 1 && digits == (resource.length - 1) -> return Float
                else -> return String
            }
        }
    }
}