package com.example.chatgptwatchrelay.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import com.example.chatgptwatchrelay.launch.ChatGptLauncher
import com.example.chatgptwatchrelay.notifications.NotificationHelper
import com.example.chatgptwatchrelay.relay.RelayDiagnostics
import com.example.chatgptwatchrelay.relay.RelayState

object ChatGptCommandSender {
    private const val MAX_ATTEMPTS = 16
    private const val TIMEOUT_MILLIS = 40_000L

    private data class PendingCommand(
        val text: String,
        val source: String,
        val createdAtMillis: Long = System.currentTimeMillis(),
        var placedInInput: Boolean = false,
        var sendAttemptedAfterPlacement: Boolean = false,
        var attempts: Int = 0,
        var lastInputBounds: Rect? = null
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
            val reason = "Could not place the command in ChatGPT or press Send."
            pendingCommand = null
            RelayDiagnostics.commandFailed(reason)
            NotificationHelper.showCommandFailureNotification(service, reason)
            return false
        }

        val root = service.rootInActiveWindow ?: return false
        val inputCandidates = findInputCandidates(root)
        RelayDiagnostics.updateCommandCandidates(inputCandidates.size, findSendCandidates(root).size)
        val inputNode = inputCandidates.lastOrNull()

        if (!pending.placedInInput) {
            if (inputNode == null) return false
            inputNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
            inputNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            pending.lastInputBounds = Rect().also { inputNode.getBoundsInScreen(it) }

            val setTextFirst = setInputText(inputNode, pending.text)
            val pasteSecond = inputNode.performAction(AccessibilityNodeInfo.ACTION_PASTE)
            val imeEnter = performImeEnter(inputNode)

            if (setTextFirst || pasteSecond || imeEnter) {
                pending.placedInInput = true
                RelayDiagnostics.commandQueued("${pending.source} command placed; waiting for Send")
                return false
            }
            return false
        }

        if (!pending.sendAttemptedAfterPlacement && inputNode != null) {
            val currentText = inputNode.text?.toString().orEmpty()
            if (currentText.isBlank()) {
                inputNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                inputNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                setInputText(inputNode, pending.text)
                performImeEnter(inputNode)
                return false
            }
        }

        val currentRoot = service.rootInActiveWindow ?: root
        val sendCandidates = findSendCandidates(currentRoot)
        RelayDiagnostics.updateCommandCandidates(inputCandidates.size, sendCandidates.size)
        val sendNode = sendCandidates.lastOrNull()
        pending.sendAttemptedAfterPlacement = true
        val sent = if (sendNode != null) {
            sendNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            tapLikelySendButton(service, currentRoot, pending.lastInputBounds)
        }

        if (sent) {
            pendingCommand = null
            RelayDiagnostics.commandSent()
            RelayState.monitoringEnabled = true
        }
        return sent
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

    private fun performImeEnter(inputNode: AccessibilityNodeInfo): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            inputNode.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.id)
        } else {
            false
        }
    }

    private fun tapLikelySendButton(
        service: AccessibilityService,
        root: AccessibilityNodeInfo,
        inputBounds: Rect?
    ): Boolean {
        val rootBounds = Rect().also { root.getBoundsInScreen(it) }
        val y = when {
            inputBounds != null && !inputBounds.isEmpty -> inputBounds.centerY().toFloat()
            else -> rootBounds.bottom - 72f
        }
        val x = rootBounds.right - 56f
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 80))
            .build()
        return service.dispatchGesture(gesture, null, null)
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
