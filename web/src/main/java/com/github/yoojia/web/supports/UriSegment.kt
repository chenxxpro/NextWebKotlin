package com.github.yoojia.web.supports

/**
 * @author Yoojia Chen (yoojiachen@gmail.com)
 * @since 2.a.2
 */
class UriSegment private constructor(val dynamic: Boolean,
                                     val wildcard: Boolean,
                                     val type: UriValueType,
                                     val fixedType: Boolean,
                                     val segment: String) {

    companion object {

        fun fromDefine(segment: String): UriSegment {
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
            // {user-id} -> user-id
            if(dynamic) {
                val unwrap = if(dynamic) segment.substring(1, segment.length-1) else segment
                when {
                    unwrap.startsWith("int:") -> {
                        valueType = UriValueType.Int
                        _segment = unwrap.substring(4)
                    }
                    unwrap.startsWith("float:") -> {
                        valueType = UriValueType.Float
                        _segment = unwrap.substring(6)
                    }
                    unwrap.startsWith("string:") -> {
                        valueType = UriValueType.String
                        _segment = unwrap.substring(7)
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

        fun fromRequest(segment: String): UriSegment {
            return UriSegment(dynamic = false,
                    wildcard = false,
                    type = UriValueType.parse(segment),
                    fixedType = true, /*请求参数的数值类型要求为绝对类型，不能为ValueType.Any*/
                    segment = segment)
        }

        /**
         * 客户端请求的UriSegments与定义的UriSegments是否匹配。
         */
        fun isRequestMatchDefine(request: List<UriSegment>, define: List<UriSegment>): Boolean {
            if(define.last().wildcard) {
                val index = define.size - 1
                if(request.size < index) {
                    return false
                }else{
                    return match(request.subList(0, index), define.subList(0, index))
                }
            }else{
                return request.size == define.size && match(request, define)
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
                if(define.dynamic) {
                    match = define.type.match(request.type)
                }else{
                    match = define.segment.equals(request.segment, ignoreCase = false)
                }
                if(!match) return false
            }
            return true
        }

    }

}