package com.example.chatgptwatchrelay.relay

object RelayState {
    var lastFullResponse: String = ""
    var chunks: List<String> = emptyList()
    var currentChunkIndex: Int = 0
    var lastFingerprint: Int = 0
    var monitoringEnabled: Boolean = false

    fun setResponse(text: String) {
        lastFullResponse = text.trim()
        chunks = Chunker.split(lastFullResponse)
        currentChunkIndex = 0
        lastFingerprint = lastFullResponse.hashCode()
    }

    fun currentChunk(): String = chunks.getOrNull(currentChunkIndex).orEmpty()

    fun advanceChunk(): Boolean {
        if (currentChunkIndex + 1 >= chunks.size) return false
        currentChunkIndex += 1
        return true
    }
}
