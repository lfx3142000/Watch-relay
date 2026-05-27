package com.example.chatgptwatchrelay.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.example.chatgptwatchrelay.notifications.NotificationHelper
import com.example.chatgptwatchrelay.relay.RelayDiagnostics
import com.example.chatgptwatchrelay.relay.RelayState

class ChatGptAccessibilityService : AccessibilityService() {
    private var lastObservedResponse = ""
    private var stableCount = 0

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
        if (responseText.length < 20) return

        if (responseText == lastObservedResponse) {
            stableCount++
        } else {
            stableCount = 0
            lastObservedResponse = responseText
        }

        val fingerprint = responseText.hashCode()
        if (stableCount >= 4 && RelayState.canNotifyResponse(fingerprint)) {
            RelayState.setResponse(responseText)
            NotificationHelper.showResponseNotification(this)
            // Prevent scrolling or small visible text changes from creating more notifications
            // for the same completed answer. Monitoring resumes when the user sends a command,
            // sends a reply, or manually starts monitoring again.
            RelayState.monitoringEnabled = false
            stableCount = 0
        }
    }

    override fun onInterrupt() = Unit

    private fun isChatGptTarget(packageName: String): Boolean {
        return packageName.contains("openai", ignoreCase = true) ||
            packageName.contains("chrome", ignoreCase = true)
    }
}
