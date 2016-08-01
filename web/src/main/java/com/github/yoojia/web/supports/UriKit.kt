package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.9
 */

/**
 * 创建模块定义的UriSegment对象。模块定义的URI有以下特征:
 * - 静态路径,如: /users/chen
 * - 动态路径,如: /users/{string:username}
 * - 全匹配路径,如: /users/\*
 */
fun createDefineUriSegment(segment: String): UriSegment {
    val starts = if(segment.startsWith('{')) 1 else 0
    val ends = if(segment.endsWith('}')) 1 else 0
    if(starts.xor(ends) == 1) {
        throw IllegalArgumentException("Invalid uri segment: $segment")
    }
    val dynamic = segment.length >= 3/*{a}*/&& starts.and(ends) == 1
    val wildcard = !dynamic && "*".equals(segment)
    val _segment: String
    val valueType: UriValueType
    var fixedType: Boolean = true
    if(dynamic) {
        // unwrap: {user-id} -> user-id
        val unwrap = if(dynamic) segment.substring(1/*{*/, segment.lastIndex/*}*/) else segment
        when {
            unwrap.startsWith(UriValueType.INT.prefix) -> {
                valueType = UriValueType.INT
                _segment = unwrap.substring(UriValueType.INT.offset)
            }
            unwrap.startsWith(UriValueType.FLOAT.prefix) -> {
                valueType = UriValueType.FLOAT
                _segment = unwrap.substring(UriValueType.FLOAT.offset)
            }
            unwrap.startsWith(UriValueType.LONG.prefix) -> {
                valueType = UriValueType.LONG
                _segment = unwrap.substring(UriValueType.LONG.offset)
            }
            unwrap.startsWith(UriValueType.DOUBLE.prefix) -> {
                valueType = UriValueType.DOUBLE
                _segment = unwrap.substring(UriValueType.DOUBLE.offset)
            }
            unwrap.startsWith(UriValueType.STRING.prefix) -> {
                valueType = UriValueType.STRING
                _segment = unwrap.substring(UriValueType.STRING.offset)
            }
            else -> {
                fixedType = false
                valueType = UriValueType.UNDEFINED
                _segment = unwrap
            }
        }
    }else {
        _segment = segment
        valueType = UriValueType.parse(_segment)
    }
    return UriSegment(dynamic, wildcard, valueType, fixedType, _segment)
}

/**
 * 从客户端Request中获取的URI,创建其UriSegment。
 */
fun createRequestUriSegment(segment: String): UriSegment {
    return UriSegment(isDynamic = false,
            isWildcard = false,
            valueType = UriValueType.parse(segment),
            isFixedType = true, /*请求参数的数值类型要求为固定类型，不能为ValueType.Any*/
            segment = segment)
}
