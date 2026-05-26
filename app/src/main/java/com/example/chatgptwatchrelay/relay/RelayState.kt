package com.example.chatgptwatchrelay.relay

object RelayState {
    var lastFullResponse: String = ""
    var chunks: List<String> = emptyList()
    var currentChunkIndex: Int = 0
    var lastFingerprint: Int = 0
    var monitoringEnabled: Boolean = false
    var notifyOnceEnabled: Boolean = true
    var hasNotifiedThisSession: Boolean = false

    fun setResponse(text: String, markNotified: Boolean = true) {
        lastFullResponse = text.trim()
        chunks = Chunker.split(lastFullResponse)
        currentChunkIndex = 0
        lastFingerprint = lastFullResponse.hashCode()
        if (markNotified) hasNotifiedThisSession = true
    }

    fun startMonitoring() {
        monitoringEnabled = true
        hasNotifiedThisSession = false
    }

    fun stopMonitoring() {
        monitoringEnabled = false
    }

    fun canNotify(): Boolean = monitoringEnabled && (!notifyOnceEnabled || !hasNotifiedThisSession)

    fun currentChunk(): String = chunks.getOrNull(currentChunkIndex).orEmpty()

    fun advanceChunk(): Boolean {
        if (currentChunkIndex + 1 >= chunks.size) return false
        currentChunkIndex += 1
        return true
    }
}
