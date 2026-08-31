package com.arman.dev.converterpro.feature.files.data.cache

internal object CachedConvertedFilesJson {

    fun encode(files: List<CachedConvertedFile>): String = buildString {
        append('[')
        files.forEachIndexed { index, file ->
            if (index > 0) append(',')
            append("{\"id\":").append(file.id)
            append(",\"name\":").append(file.name.toJsonString())
            append(",\"sizeBytes\":").append(file.sizeBytes)
            append(",\"durationMs\":").append(file.durationMs)
            append(",\"bitrateKbps\":").append(file.bitrateKbps ?: "null")
            append(",\"channels\":").append(file.channels ?: "null")
            append('}')
        }
        append(']')
    }

    fun decode(json: String): List<CachedConvertedFile> {
        val trimmed = json.trim()
        if (trimmed.isEmpty()) return emptyList()
        return JsonObjectArrayParser(trimmed).parse().map { obj ->
            CachedConvertedFile(
                id = obj.requiredLong("id"),
                name = obj.requiredString("name"),
                sizeBytes = obj.requiredLong("sizeBytes"),
                durationMs = obj.requiredLong("durationMs"),
                bitrateKbps = obj.intOrNull("bitrateKbps"),
                channels = obj.intOrNull("channels")
            )
        }
    }
}

private fun String.toJsonString(): String = buildString {
    append('"')
    for (char in this@toJsonString) {
        when (char) {
            '"' -> append("\\\"")
            '\\' -> append("\\\\")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(char)
        }
    }
    append('"')
}

private class JsonObjectArrayParser(private val source: String) {
    private var index = 0

    fun parse(): List<Map<String, Any?>> {
        skipWhitespace()
        if (index >= source.length) return emptyList()
        expect('[')
        skipWhitespace()
        if (peek() == ']') {
            index++
            return emptyList()
        }
        val items = mutableListOf<Map<String, Any?>>()
        while (true) {
            items += parseObject()
            skipWhitespace()
            when (peek()) {
                ',' -> {
                    index++
                    skipWhitespace()
                }
                ']' -> {
                    index++
                    return items
                }
                else -> error("Malformed converted-files cache.")
            }
        }
    }

    private fun parseObject(): Map<String, Any?> {
        expect('{')
        skipWhitespace()
        if (peek() == '}') {
            index++
            return emptyMap()
        }
        val obj = mutableMapOf<String, Any?>()
        while (true) {
            val key = parseString()
            skipWhitespace()
            expect(':')
            skipWhitespace()
            obj[key] = parseValue()
            skipWhitespace()
            when (peek()) {
                ',' -> {
                    index++
                    skipWhitespace()
                }
                '}' -> {
                    index++
                    return obj
                }
                else -> error("Malformed converted-files cache.")
            }
        }
    }

    private fun parseValue(): Any? = when (val char = peek()) {
        '"' -> parseString()
        'n' -> {
            expectWord("null")
            null
        }
        '-', in '0'..'9' -> parseNumber()
        else -> error("Unexpected value starting with '$char'.")
    }

    private fun parseString(): String {
        expect('"')
        val decoded = StringBuilder()
        while (index < source.length) {
            when (val char = source[index++]) {
                '"' -> return decoded.toString()
                '\\' -> {
                    if (index >= source.length) error("Unterminated escape in cache JSON.")
                    decoded.append(
                        when (val escaped = source[index++]) {
                            '"' -> '"'
                            '\\' -> '\\'
                            'n' -> '\n'
                            'r' -> '\r'
                            't' -> '\t'
                            else -> escaped
                        }
                    )
                }
                else -> decoded.append(char)
            }
        }
        error("Unterminated string in cache JSON.")
    }

    private fun parseNumber(): Long {
        val start = index
        if (peek() == '-') index++
        while (index < source.length && source[index] in '0'..'9') index++
        return source.substring(start, index).toLong()
    }

    private fun expect(char: Char) {
        if (peek() != char) error("Expected '$char' in cache JSON.")
        index++
    }

    private fun expectWord(word: String) {
        if (!source.startsWith(word, index)) error("Expected '$word' in cache JSON.")
        index += word.length
    }

    private fun peek(): Char {
        if (index >= source.length) error("Unexpected end of cache JSON.")
        return source[index]
    }

    private fun skipWhitespace() {
        while (index < source.length && source[index].isWhitespace()) index++
    }
}

private fun Map<String, Any?>.requiredLong(key: String): Long = when (val value = get(key)) {
    is Long -> value
    else -> error("Missing $key in converted-files cache.")
}

private fun Map<String, Any?>.requiredString(key: String): String = when (val value = get(key)) {
    is String -> value
    else -> error("Missing $key in converted-files cache.")
}

private fun Map<String, Any?>.intOrNull(key: String): Int? = when (val value = get(key)) {
    is Long -> value.toInt()
    else -> null
}
