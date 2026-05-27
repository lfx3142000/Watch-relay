package com.example.chatgptwatchrelay.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import com.example.chatgptwatchrelay.launch.ChatGptLauncher
import com.example.chatgptwatchrelay.notifications.NotificationHelper
import com.example.chatgptwatchrelay.relay.RelayDiagnostics
import com.example.chatgptwatchrelay.relay.RelayState

object ChatGptCommandSender {
    private const val MAX_ATTEMPTS = 22
    private const val TIMEOUT_MILLIS = 50_000L

    private data class PendingCommand(
        val text: String,
        val source: String,
        val createdAtMillis: Long = System.currentTimeMillis(),
        var focusTapped: Boolean = false,
        var pasteAttempted: Boolean = false,
        var verifiedInInput: Boolean = false,
        var sendAttemptedAfterVerification: Boolean = false,
        var attempts: Int = 0,
        var lastInputText: String = ""
    )

    private var pendingCommand: PendingCommand? = null

    fun queueCommand(context: Context, text: String, source: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        copyToClipboard(context, trimmed, source)
        pendingCommand = PendingCommand(text = trimmed, source = source)
        RelayDiagnostics.commandQueued(source)
        RelayState.monitoringEnabled = false
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
            val reason = "Could not paste the command into ChatGPT or press Send."
            pendingCommand = null
            RelayDiagnostics.commandFailed(reason)
            NotificationHelper.showCommandFailureNotification(service, reason)
            return false
        }

        val root = service.rootInActiveWindow ?: return false
        val inputCandidates = findInputCandidates(root)
        val sendCandidates = findExplicitSendCandidates(root)
        RelayDiagnostics.updateCommandCandidates(inputCandidates.size, sendCandidates.size)
        val inputNode = chooseInputCandidate(inputCandidates)

        if (inputNode != null) {
            pending.lastInputText = inputNode.text?.toString().orEmpty().trim()
            if (inputLooksPopulated(inputNode, pending.text)) {
                pending.verifiedInInput = true
            }
        }

        if (!pending.focusTapped) {
            if (inputNode == null) return false
            inputNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            inputNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            pending.focusTapped = true
            RelayDiagnostics.commandQueued("${pending.source} focused ChatGPT input")
            return false
        }

        if (!pending.verifiedInInput) {
            if (inputNode == null) return false
            inputNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            inputNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)

            if (!pending.pasteAttempted) {
                val pasted = inputNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)
                pending.pasteAttempted = true

                if (!pasted && pending.attempts >= 8) {
                    setInputText(inputNode, pending.text)
                }

                RelayDiagnostics.commandQueued("${pending.source} paste attempted once; waiting for send button")
            } else {
                RelayDiagnostics.commandQueued("${pending.source} waiting for ChatGPT send button")
            }
            return false
        }

        if (sendCandidates.isEmpty()) {
            RelayDiagnostics.commandQueued("${pending.source} waiting for explicit Send button")
            return false
        }

        val sendNode = chooseSendCandidate(sendCandidates)
        pending.sendAttemptedAfterVerification = true
        val sent = sendNode?.let { clickNodeOrClickableParent(it) } == true

        if (sent) {
            pendingCommand = null
            RelayDiagnostics.commandSent()
            RelayState.startCaptureAfterSend()
        } else {
            RelayDiagnostics.commandQueued("${pending.source} Send button found but click failed")
        }
        return sent
    }

    private fun chooseInputCandidate(candidates: List<AccessibilityNodeInfo>): AccessibilityNodeInfo? {
        return candidates.lastOrNull { it.isEditable } ?: candidates.lastOrNull()
    }

    private fun chooseSendCandidate(candidates: List<AccessibilityNodeInfo>): AccessibilityNodeInfo? {
        return candidates.lastOrNull()
    }

    private fun inputLooksPopulated(inputNode: AccessibilityNodeInfo, commandText: String): Boolean {
        val currentText = inputNode.text?.toString().orEmpty().trim()
        if (currentText.isBlank()) return false
        val commandStart = commandText.take(24)
        return currentText.contains(commandStart, ignoreCase = true) ||
            commandText.contains(currentText.take(24), ignoreCase = true)
    }

    private fun setInputText(inputNode: AccessibilityNodeInfo, text: String): Boolean {
        val args = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text
            )
        }
        return inputNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    private fun clickNodeOrClickableParent(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        repeat(4) {
            val target = current ?: return false
            if (target.isVisibleToUser && target.isEnabled && target.isClickable) {
                if (target.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            }
            current = target.parent
        }
        return false
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

    private fun findExplicitSendCandidates(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val candidates = mutableListOf<AccessibilityNodeInfo>()
        root.visit { node ->
            if (!node.isVisibleToUser || !node.isEnabled) return@visit
            val text = node.text?.toString().orEmpty()
            val desc = node.contentDescription?.toString().orEmpty()
            val label = "$text $desc".trim()
            val lower = label.lowercase()

            val isSend = lower == "send" ||
                lower == "submit" ||
                lower.contains("send message") ||
                lower.contains("submit message") ||
                lower.contains("send prompt")

            val isVoice = lower.contains("voice") ||
                lower.contains("microphone") ||
                lower.contains("dictate") ||
                lower.contains("record")

            if (isSend && !isVoice) {
                candidates += node
            }
        }
        return candidates
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
