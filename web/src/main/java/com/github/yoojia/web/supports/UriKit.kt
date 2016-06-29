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
            unwrap.startsWith(UriValueType.Int.prefix) -> {
                valueType = UriValueType.Int
                _segment = unwrap.substring(UriValueType.Int.offset)
            }
            unwrap.startsWith(UriValueType.Float.prefix) -> {
                valueType = UriValueType.Float
                _segment = unwrap.substring(UriValueType.Float.offset)
            }
            unwrap.startsWith(UriValueType.String.prefix) -> {
                valueType = UriValueType.String
                _segment = unwrap.substring(UriValueType.String.offset)
            }
            else -> {
                fixedType = false
                valueType = UriValueType.Any
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

/**
 * 客户端请求的UriSegments与定义的UriSegments是否匹配。
 */
fun isUriSegmentMatch(requests: List<UriSegment>, defines: List<UriSegment>): Boolean {
    if(defines.last().isWildcard) {
        val index = defines.size - 1
        if(requests.size < index) {
            return false
        }else{
            return match(requests.subList(0, index), defines.subList(0, index))
        }
    }else{
        return requests.size == defines.size && match(requests, defines)
    }
}

/**
 * 在UriSegment资源长度相同的情况下，判断它们是否匹配；
 * - 定义为动态参数：比较它们的类型是否相同，忽略资源名；定义为字符串类型时，可以匹配任意请求资源类型；
 * - 定义为静态字段：比较资源名是否相同（大小写完全相同）；
 */
private fun match(requests: List<UriSegment>, defines: List<UriSegment>): Boolean{
    for(i in requests.indices) {
        val define = defines[i]
        val request = requests[i]
        val match: Boolean
        if(define.isDynamic) {
            match = define.valueType.match(request.valueType)
        }else{
            match = define.segment.equals(request.segment, ignoreCase = false)
        }
        if(!match) return false
    }
    return true
}