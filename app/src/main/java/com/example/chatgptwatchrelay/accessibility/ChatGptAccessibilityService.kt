package com.example.chatgptwatchrelay.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.chatgptwatchrelay.notifications.NotificationHelper
import com.example.chatgptwatchrelay.relay.RelayDiagnostics
import com.example.chatgptwatchrelay.relay.RelayState
import com.example.chatgptwatchrelay.relay.ResponseCaptureState

class ChatGptAccessibilityService : AccessibilityService() {
    private var observedSessionId = -1L
    private var baselineResponse = ""
    private var bestCandidateResponse = ""
    private var stableEndControlCount = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString().orEmpty()
        if (!isChatGptTarget(packageName)) return

        if (ChatGptCommandSender.hasPendingCommand()) {
            ChatGptCommandSender.trySendPendingCommand(this)
            return
        }

        if (!RelayState.monitoringEnabled) return

        val snapshot = ChatGptScreenReader.read(rootInActiveWindow)
        RelayDiagnostics.updateScreenSnapshot(packageName, snapshot.allVisibleText)
        val responseText = snapshot.likelyLatestResponse.trim()
        RelayDiagnostics.updateLikelyResponse(responseText, snapshot.responseLineCount)

        if (observedSessionId != RelayState.monitoringSessionId) {
            observedSessionId = RelayState.monitoringSessionId
            baselineResponse = responseText
            bestCandidateResponse = ""
            stableEndControlCount = 0
            RelayDiagnostics.commandQueued("Response capture armed")
            return
        }

        if (responseText.length < MIN_RESPONSE_CHARS) return

        when (RelayState.captureState) {
            ResponseCaptureState.IDLE,
            ResponseCaptureState.NOTIFIED -> return

            ResponseCaptureState.WAITING_FOR_NEW_RESPONSE -> {
                if (isMeaningfullyDifferent(responseText, baselineResponse)) {
                    bestCandidateResponse = responseText
                    stableEndControlCount = 0
                    RelayState.markCapturing()
                    RelayDiagnostics.commandQueued("Capturing new response")
                }
            }

            ResponseCaptureState.CAPTURING -> {
                if (responseText.length > bestCandidateResponse.length || !bestCandidateResponse.contains(responseText)) {
                    bestCandidateResponse = responseText
                    stableEndControlCount = 0
                }

                if (snapshot.hasResponseEndControls && bestCandidateResponse.length >= MIN_RESPONSE_CHARS) {
                    stableEndControlCount++
                } else {
                    stableEndControlCount = 0
                }

                val fingerprint = bestCandidateResponse.hashCode()
                if (stableEndControlCount >= END_CONTROL_STABLE_EVENTS && RelayState.canNotifyResponse(fingerprint)) {
                    RelayState.setResponse(bestCandidateResponse)
                    NotificationHelper.showResponseNotification(this)
                    RelayState.monitoringEnabled = false
                    stableEndControlCount = 0
                }
            }
        }
    }

    override fun onInterrupt() = Unit

    private fun isMeaningfullyDifferent(current: String, baseline: String): Boolean {
        if (baseline.isBlank()) return current.length >= MIN_RESPONSE_CHARS
        val currentNormalized = current.normalizeForCompare()
        val baselineNormalized = baseline.normalizeForCompare()
        if (currentNormalized == baselineNormalized) return false
        return currentNormalized.length >= MIN_RESPONSE_CHARS &&
            !baselineNormalized.contains(currentNormalized) &&
            !currentNormalized.endsWith(baselineNormalized)
    }

    private fun String.normalizeForCompare(): String =
        lowercase().replace(Regex("\\s+"), " ").trim()

    private fun isChatGptTarget(packageName: String): Boolean {
        return packageName.contains("openai", ignoreCase = true) ||
            packageName.contains("chrome", ignoreCase = true)
    }

    companion object {
        private const val MIN_RESPONSE_CHARS = 40
        private const val END_CONTROL_STABLE_EVENTS = 2
    }
}
