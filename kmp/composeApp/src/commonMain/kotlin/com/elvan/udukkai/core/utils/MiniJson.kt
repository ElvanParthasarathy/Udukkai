package com.elvan.udukkai.core.utils

/**
 * Minimal JSON reader/writer (the project has no JSON library).
 * Objects become Map<String, Any?>, arrays List<Any?>, numbers Double.
 */
object MiniJson {

    fun parse(text: String): Any? = Parser(text).run {
        val value = readValue()
        skipWhitespace()
        value
    }

    @Suppress("UNCHECKED_CAST")
    fun parseObject(text: String?): Map<String, Any?> {
        if (text.isNullOrBlank()) return emptyMap()
        return try {
            parse(text) as? Map<String, Any?> ?: emptyMap()
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun stringify(value: Any?): String = StringBuilder().also { write(it, value) }.toString()

    private fun write(sb: StringBuilder, value: Any?) {
        when (value) {
            null -> sb.append("null")
            is String -> writeString(sb, value)
            is Boolean -> sb.append(value)
            is Double -> sb.append(if (value.isFinite()) formatNumber(value) else "0")
            is Number -> sb.append(value.toString())
            is Map<*, *> -> {
                sb.append('{')
                var first = true
                for ((k, v) in value) {
                    if (!first) sb.append(',')
                    first = false
                    writeString(sb, k.toString())
                    sb.append(':')
                    write(sb, v)
                }
                sb.append('}')
            }
            is List<*> -> {
                sb.append('[')
                value.forEachIndexed { i, v ->
                    if (i > 0) sb.append(',')
                    write(sb, v)
                }
                sb.append(']')
            }
            else -> writeString(sb, value.toString())
        }
    }

    private fun formatNumber(d: Double): String =
        if (d == kotlin.math.floor(d) && kotlin.math.abs(d) < 1e15) d.toLong().toString() else d.toString()

    private fun writeString(sb: StringBuilder, s: String) {
        sb.append('"')
        for (ch in s) {
            when {
                ch == '"' -> sb.append("\\\"")
                ch == '\\' -> sb.append("\\\\")
                ch == '\n' -> sb.append("\\n")
                ch == '\r' -> sb.append("\\r")
                ch == '\t' -> sb.append("\\t")
                ch < ' ' || ch == ' ' || ch == ' ' ->
                    sb.append("\\u").append(ch.code.toString(16).padStart(4, '0'))
                else -> sb.append(ch)
            }
        }
        sb.append('"')
    }

    private class Parser(private val s: String) {
        private var i = 0

        fun skipWhitespace() {
            while (i < s.length && s[i].isWhitespace()) i++
        }

        fun readValue(): Any? {
            skipWhitespace()
            if (i >= s.length) throw IllegalArgumentException("Unexpected end of JSON")
            return when (val c = s[i]) {
                '{' -> readObject()
                '[' -> readArray()
                '"' -> readString()
                't' -> literal("true", true)
                'f' -> literal("false", false)
                'n' -> literal("null", null)
                else -> if (c == '-' || c.isDigit()) readNumber() else throw IllegalArgumentException("Bad JSON at $i")
            }
        }

        private fun literal(word: String, value: Any?): Any? {
            if (!s.startsWith(word, i)) throw IllegalArgumentException("Bad JSON at $i")
            i += word.length
            return value
        }

        private fun readObject(): Map<String, Any?> {
            val map = LinkedHashMap<String, Any?>()
            i++
            skipWhitespace()
            if (s[i] == '}') { i++; return map }
            while (true) {
                skipWhitespace()
                val key = readString()
                skipWhitespace()
                expect(':')
                map[key] = readValue()
                skipWhitespace()
                if (s[i] == ',') { i++; continue }
                expect('}')
                return map
            }
        }

        private fun readArray(): List<Any?> {
            val list = ArrayList<Any?>()
            i++
            skipWhitespace()
            if (s[i] == ']') { i++; return list }
            while (true) {
                list.add(readValue())
                skipWhitespace()
                if (s[i] == ',') { i++; continue }
                expect(']')
                return list
            }
        }

        private fun readString(): String {
            expect('"')
            val sb = StringBuilder()
            while (true) {
                val c = s[i++]
                when (c) {
                    '"' -> return sb.toString()
                    '\\' -> {
                        when (val e = s[i++]) {
                            'n' -> sb.append('\n')
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\u000C')
                            'u' -> { sb.append(s.substring(i, i + 4).toInt(16).toChar()); i += 4 }
                            else -> sb.append(e)
                        }
                    }
                    else -> sb.append(c)
                }
            }
        }

        private fun readNumber(): Double {
            val start = i
            while (i < s.length && (s[i].isDigit() || s[i] in "+-.eE")) i++
            return s.substring(start, i).toDouble()
        }

        private fun expect(c: Char) {
            if (i >= s.length || s[i] != c) throw IllegalArgumentException("Expected '$c' at $i")
            i++
        }
    }
}
