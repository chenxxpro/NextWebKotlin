package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.2
 */
class UriSegment(@JvmField val isDynamic: Boolean,
                 @JvmField val isWildcard: Boolean,
                 @JvmField val valueType: UriValueType,
                 @JvmField val isFixedType: Boolean,
                 @JvmField val segment: String) {
}