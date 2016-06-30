package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.2
 */
class UriSegment(val isDynamic: Boolean,
                 val isWildcard: Boolean,
                 val valueType: UriValueType,
                 val isFixedType: Boolean,
                 val segment: String) {
}