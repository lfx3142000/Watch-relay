package com.example.chatgptwatchrelay.relay

object Chunker {
    fun split(text: String, maxChunkSize: Int = 850): List<String> {
        val normalized = text.trim().replace(Regex("\\s+"), " ")
        if (normalized.isEmpty()) return emptyList()
        if (normalized.length <= maxChunkSize) return listOf(normalized)

        val chunks = mutableListOf<String>()
        var start = 0
        while (start < normalized.length) {
            var end = (start + maxChunkSize).coerceAtMost(normalized.length)
            if (end < normalized.length) {
                val lastSpace = normalized.lastIndexOf(' ', end)
                if (lastSpace > start + maxChunkSize / 2) end = lastSpace
            }
            chunks += normalized.substring(start, end).trim()
            start = end
            while (start < normalized.length && normalized[start].isWhitespace()) start++
        }
        return chunks
    }
}
