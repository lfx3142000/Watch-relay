package com.example.chatgptwatchrelay.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.chatgptwatchrelay.notifications.NotificationHelper
import com.example.chatgptwatchrelay.relay.RelayDiagnostics
import com.example.chatgptwatchrelay.relay.RelayState

class ChatGptAccessibilityService : AccessibilityService() {
    private var lastObservedText = ""
    private var stableCount = 0

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString().orEmpty()
        if (!isChatGptTarget(packageName)) return

        if (ChatGptCommandSender.hasPendingCommand()) {
            ChatGptCommandSender.trySendPendingCommand(this)
            return
        }

        if (!RelayState.monitoringEnabled) return

        val visibleText = rootInActiveWindow?.collectText().orEmpty().trim()
        RelayDiagnostics.updateScreenSnapshot(packageName, visibleText)
        if (visibleText.length < 20) return

        if (visibleText == lastObservedText) {
            stableCount++
        } else {
            stableCount = 0
            lastObservedText = visibleText
        }

        if (stableCount >= 4 && visibleText.hashCode() != RelayState.lastFingerprint) {
            RelayState.setResponse(visibleText)
            NotificationHelper.showResponseNotification(this)
        }
    }

    override fun onInterrupt() = Unit

    private fun isChatGptTarget(packageName: String): Boolean {
        return packageName.contains("openai", ignoreCase = true) ||
            packageName.contains("chrome", ignoreCase = true)
    }

    private fun AccessibilityNodeInfo.collectText(): String {
        val output = StringBuilder()
        fun visit(node: AccessibilityNodeInfo?) {
            if (node == null) return
            val text = node.text?.toString()?.trim()
            if (!text.isNullOrBlank()) {
                output.append(text).append('\n')
            }
            for (i in 0 until node.childCount) visit(node.getChild(i))
        }
        visit(this)
        return output.toString()
    }
}
