package com.example.chatgptwatchrelay.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.chatgptwatchrelay.notifications.NotificationHelper
import com.example.chatgptwatchrelay.relay.RelayDiagnostics
import com.example.chatgptwatchrelay.relay.RelayState

class ChatGptAccessibilityService : AccessibilityService() {
    private var observedSessionId = -1L
    private var baselineResponse = ""
    private var candidateResponse = ""
    private var stableCount = 0
    private var sawResponseChangeAfterBaseline = false

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
        if (responseText.length < MIN_RESPONSE_CHARS) return

        if (observedSessionId != RelayState.monitoringSessionId) {
            observedSessionId = RelayState.monitoringSessionId
            baselineResponse = responseText
            candidateResponse = ""
            stableCount = 0
            sawResponseChangeAfterBaseline = false
            RelayDiagnostics.commandQueued("Monitoring baseline captured")
            return
        }

        if (!sawResponseChangeAfterBaseline) {
            if (isMeaningfullyDifferent(responseText, baselineResponse)) {
                sawResponseChangeAfterBaseline = true
                candidateResponse = responseText
                stableCount = 0
                RelayDiagnostics.commandQueued("New response candidate detected")
            }
            return
        }

        if (responseText == candidateResponse) {
            stableCount++
        } else {
            candidateResponse = responseText
            stableCount = 0
        }

        val fingerprint = candidateResponse.hashCode()
        if (stableCount >= STABLE_EVENT_THRESHOLD && RelayState.canNotifyResponse(fingerprint)) {
            RelayState.setResponse(candidateResponse)
            NotificationHelper.showResponseNotification(this)
            RelayState.monitoringEnabled = false
            stableCount = 0
        }
    }

    override fun onInterrupt() = Unit

    private fun isMeaningfullyDifferent(current: String, baseline: String): Boolean {
        if (baseline.isBlank()) return current.length >= MIN_RESPONSE_CHARS
        if (current == baseline) return false
        val currentNormalized = current.normalizeForCompare()
        val baselineNormalized = baseline.normalizeForCompare()
        if (currentNormalized == baselineNormalized) return false
        if (currentNormalized.contains(baselineNormalized) && currentNormalized.length - baselineNormalized.length < MIN_NEW_TEXT_CHARS) {
            return false
        }
        if (baselineNormalized.contains(currentNormalized) && baselineNormalized.length - currentNormalized.length < MIN_NEW_TEXT_CHARS) {
            return false
        }
        return currentNormalized.length >= MIN_RESPONSE_CHARS &&
            kotlin.math.abs(currentNormalized.length - baselineNormalized.length) >= MIN_NEW_TEXT_CHARS
    }

    private fun String.normalizeForCompare(): String =
        lowercase().replace(Regex("\\s+"), " ").trim()

    private fun isChatGptTarget(packageName: String): Boolean {
        return packageName.contains("openai", ignoreCase = true) ||
            packageName.contains("chrome", ignoreCase = true)
    }

    companion object {
        private const val MIN_RESPONSE_CHARS = 40
        private const val MIN_NEW_TEXT_CHARS = 30
        private const val STABLE_EVENT_THRESHOLD = 5
    }
}
