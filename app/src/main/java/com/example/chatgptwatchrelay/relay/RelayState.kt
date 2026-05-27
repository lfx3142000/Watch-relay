package com.example.chatgptwatchrelay.relay

object RelayState {
    var lastFullResponse: String = ""
    var chunks: List<String> = emptyList()
    var currentChunkIndex: Int = 0
    var lastFingerprint: Int = 0
    var monitoringEnabled: Boolean = false
    var notifyOnceEnabled: Boolean = true
    var hasNotifiedThisSession: Boolean = false
    var lastNotifiedFingerprint: Int = 0
    var monitoringSessionId: Long = 0L

    fun setResponse(text: String, markNotified: Boolean = true) {
        lastFullResponse = text.trim()
        chunks = Chunker.split(lastFullResponse)
        currentChunkIndex = 0
        lastFingerprint = lastFullResponse.hashCode()
        if (markNotified) {
            hasNotifiedThisSession = true
            lastNotifiedFingerprint = lastFingerprint
        }
    }

    fun startMonitoring() {
        monitoringEnabled = true
        hasNotifiedThisSession = false
        monitoringSessionId++
    }

    fun stopMonitoring() {
        monitoringEnabled = false
    }

    fun stopNotifications() {
        monitoringEnabled = false
        hasNotifiedThisSession = false
        monitoringSessionId++
    }

    fun canNotifyResponse(fingerprint: Int): Boolean {
        if (!monitoringEnabled) return false
        if (notifyOnceEnabled && hasNotifiedThisSession) return false
        return !notifyOnceEnabled || fingerprint != lastNotifiedFingerprint
    }

    fun currentChunk(): String = chunks.getOrNull(currentChunkIndex).orEmpty()

    fun advanceChunk(): Boolean {
        if (currentChunkIndex + 1 >= chunks.size) return false
        currentChunkIndex += 1
        return true
    }
}
