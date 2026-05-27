package com.example.chatgptwatchrelay.relay

enum class ResponseCaptureState {
    IDLE,
    WAITING_FOR_NEW_RESPONSE,
    CAPTURING,
    NOTIFIED
}

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
    var captureState: ResponseCaptureState = ResponseCaptureState.IDLE

    fun setResponse(text: String, markNotified: Boolean = true) {
        lastFullResponse = text.trim()
        chunks = Chunker.split(lastFullResponse)
        currentChunkIndex = 0
        lastFingerprint = lastFullResponse.hashCode()
        if (markNotified) {
            hasNotifiedThisSession = true
            lastNotifiedFingerprint = lastFingerprint
            captureState = ResponseCaptureState.NOTIFIED
        }
    }

    fun startMonitoring() {
        monitoringEnabled = true
        hasNotifiedThisSession = false
        monitoringSessionId++
        captureState = ResponseCaptureState.WAITING_FOR_NEW_RESPONSE
    }

    fun startCaptureAfterSend() {
        monitoringEnabled = true
        hasNotifiedThisSession = false
        monitoringSessionId++
        captureState = ResponseCaptureState.WAITING_FOR_NEW_RESPONSE
    }

    fun markCapturing() {
        captureState = ResponseCaptureState.CAPTURING
    }

    fun stopMonitoring() {
        monitoringEnabled = false
        captureState = ResponseCaptureState.IDLE
    }

    fun stopNotifications() {
        monitoringEnabled = false
        hasNotifiedThisSession = false
        monitoringSessionId++
        captureState = ResponseCaptureState.IDLE
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
