package com.example.chatgptwatchrelay.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import com.example.chatgptwatchrelay.launch.ChatGptLauncher
import com.example.chatgptwatchrelay.notifications.NotificationHelper
import com.example.chatgptwatchrelay.relay.RelayDiagnostics
import com.example.chatgptwatchrelay.relay.RelayState

object ChatGptCommandSender {
    private const val MAX_ATTEMPTS = 12
    private const val TIMEOUT_MILLIS = 30_000L

    private data class PendingCommand(
        val text: String,
        val source: String,
        val createdAtMillis: Long = System.currentTimeMillis(),
        var pasted: Boolean = false,
        var attempts: Int = 0
    )

    private var pendingCommand: PendingCommand? = null

    fun queueCommand(context: Context, text: String, source: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        copyToClipboard(context, trimmed, source)
        pendingCommand = PendingCommand(text = trimmed, source = source)
        RelayDiagnostics.commandQueued(source)
        RelayState.monitoringEnabled = true
        ChatGptLauncher.open(context)
    }

    fun hasPendingCommand(): Boolean = pendingCommand != null

    fun clearPendingCommand() {
        pendingCommand = null
        RelayDiagnostics.commandFailed("Cleared")
    }

    fun trySendPendingCommand(service: AccessibilityService): Boolean {
        val pending = pendingCommand ?: return false
        pending.attempts += 1

        if (pending.attempts > MAX_ATTEMPTS || System.currentTimeMillis() - pending.createdAtMillis > TIMEOUT_MILLIS) {
            val reason = "Could not find the ChatGPT input or send button."
            pendingCommand = null
            RelayDiagnostics.commandFailed(reason)
            NotificationHelper.showCommandFailureNotification(service, reason)
            return false
        }

        val root = service.rootInActiveWindow ?: return false

        if (!pending.pasted) {
            val inputCandidates = findInputCandidates(root)
            RelayDiagnostics.updateCommandCandidates(inputCandidates.size, findSendCandidates(root).size)
            val inputNode = inputCandidates.lastOrNull() ?: return false
            inputNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            inputNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)

            val pasted = inputNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)
            val setText = if (!pasted) {
                val args = Bundle().apply {
                    putCharSequence(
                        AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                        pending.text
                    )
                }
                inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            } else {
                false
            }

            if (pasted || setText) {
                pending.pasted = true
            } else {
                return false
            }
        }

        val currentRoot = service.rootInActiveWindow ?: root
        val sendCandidates = findSendCandidates(currentRoot)
        RelayDiagnostics.updateCommandCandidates(RelayDiagnostics.lastInputCandidateCount, sendCandidates.size)
        val sendNode = sendCandidates.lastOrNull() ?: return false
        val clicked = sendNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        if (clicked) {
            pendingCommand = null
            RelayDiagnostics.commandSent()
            RelayState.monitoringEnabled = true
        }
        return clicked
    }

    private fun copyToClipboard(context: Context, text: String, source: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("ChatGPT $source", text))
    }

    private fun findInputCandidates(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val candidates = mutableListOf<AccessibilityNodeInfo>()
        root.visit { node ->
            val className = node.className?.toString().orEmpty()
            val text = node.text?.toString().orEmpty()
            val hint = node.hintText?.toString().orEmpty()
            val desc = node.contentDescription?.toString().orEmpty()
            val looksLikeMessageBox = listOf(text, hint, desc).any {
                it.contains("message", ignoreCase = true) ||
                    it.contains("ask", ignoreCase = true) ||
                    it.contains("prompt", ignoreCase = true)
            }

            if ((node.isEditable || className.contains("EditText", ignoreCase = true) || looksLikeMessageBox) && node.isVisibleToUser) {
                candidates += node
            }
        }
        return candidates
    }

    private fun findSendCandidates(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val explicitCandidates = mutableListOf<AccessibilityNodeInfo>()
        val fallbackCandidates = mutableListOf<AccessibilityNodeInfo>()
        val rootBounds = Rect().also { root.getBoundsInScreen(it) }
        val rootHeight = rootBounds.height().coerceAtLeast(1)
        val rootWidth = rootBounds.width().coerceAtLeast(1)

        root.visit { node ->
            if (!node.isVisibleToUser || !node.isClickable) return@visit
            val text = node.text?.toString().orEmpty()
            val desc = node.contentDescription?.toString().orEmpty()
            val className = node.className?.toString().orEmpty()
            val label = "$text $desc"

            if (label.contains("send", ignoreCase = true) || label.contains("submit", ignoreCase = true)) {
                explicitCandidates += node
                return@visit
            }

            val bounds = Rect().also { node.getBoundsInScreen(it) }
            val isBottomArea = bounds.top > rootBounds.top + (rootHeight * 0.55)
            val isRightArea = bounds.left > rootBounds.left + (rootWidth * 0.55)
            val isButtonish = className.contains("Button", ignoreCase = true) ||
                className.contains("Image", ignoreCase = true) ||
                desc.isNotBlank()
            val reasonableSize = bounds.width() in 24..260 && bounds.height() in 24..260

            if (isBottomArea && isRightArea && isButtonish && reasonableSize) {
                fallbackCandidates += node
            }
        }
        return if (explicitCandidates.isNotEmpty()) explicitCandidates else fallbackCandidates
    }

    private fun AccessibilityNodeInfo.visit(block: (AccessibilityNodeInfo) -> Unit) {
        fun recurse(node: AccessibilityNodeInfo?) {
            if (node == null) return
            block(node)
            for (i in 0 until node.childCount) {
                recurse(node.getChild(i))
            }
        }
        recurse(this)
    }
}
