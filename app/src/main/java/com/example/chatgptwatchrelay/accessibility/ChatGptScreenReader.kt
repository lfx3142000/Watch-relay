package com.example.chatgptwatchrelay.accessibility

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

object ChatGptScreenReader {
    data class ScreenSnapshot(
        val allVisibleText: String,
        val likelyLatestResponse: String,
        val visibleLineCount: Int,
        val responseLineCount: Int,
        val hasResponseEndControls: Boolean,
        val hasGeneratingControl: Boolean,
        val sentCommandBottomY: Int
    )

    private data class UiLine(
        val text: String,
        val top: Int,
        val bottom: Int
    )

    fun read(root: AccessibilityNodeInfo?, sentCommandText: String = ""): ScreenSnapshot {
        val uiLines = collectLines(root)
        val lines = uiLines.map { it.text }
        val allText = lines.joinToString("\n")
        val sentCommandBottomY = findSentCommandBottomY(uiLines, sentCommandText)
        val scopedLines = if (sentCommandBottomY > 0) {
            uiLines.filter { it.top >= sentCommandBottomY - COMMAND_Y_TOLERANCE_PX }.map { it.text }
        } else {
            lines
        }
        val responseLines = extractLikelyResponseLines(scopedLines)
        return ScreenSnapshot(
            allVisibleText = allText,
            likelyLatestResponse = responseLines.joinToString("\n").trim(),
            visibleLineCount = lines.size,
            responseLineCount = responseLines.size,
            hasResponseEndControls = hasResponseEndControlsBelow(uiLines, sentCommandBottomY),
            hasGeneratingControl = hasGeneratingControl(lines),
            sentCommandBottomY = sentCommandBottomY
        )
    }

    private fun collectLines(root: AccessibilityNodeInfo?): List<UiLine> {
        val lines = mutableListOf<UiLine>()
        fun visit(node: AccessibilityNodeInfo?) {
            if (node == null) return
            if (node.isVisibleToUser) {
                val rect = Rect()
                node.getBoundsInScreen(rect)
                val text = node.text?.toString()?.trim().orEmpty()
                val desc = node.contentDescription?.toString()?.trim().orEmpty()
                if (text.isNotBlank()) lines += UiLine(text, rect.top, rect.bottom)
                if (desc.isNotBlank() && desc != text) lines += UiLine(desc, rect.top, rect.bottom)
            }
            for (i in 0 until node.childCount) visit(node.getChild(i))
        }
        visit(root)
        return lines
            .flatMap { line -> line.text.lines().map { UiLine(it.trim(), line.top, line.bottom) } }
            .filter { it.text.isNotBlank() }
            .sortedWith(compareBy<UiLine> { it.top }.thenBy { it.bottom }.thenBy { it.text })
            .distinctConsecutiveByText()
    }

    private fun findSentCommandBottomY(lines: List<UiLine>, sentCommandText: String): Int {
        val needle = sentCommandText.normalizeForCompare()
        if (needle.isBlank()) return 0
        val shortNeedle = needle.take(60)
        return lines
            .filter { line ->
                val hay = line.text.normalizeForCompare()
                hay.contains(shortNeedle) || needle.contains(hay.take(60))
            }
            .maxOfOrNull { it.bottom }
            ?: 0
    }

    private fun extractLikelyResponseLines(lines: List<String>): List<String> {
        val filtered = lines.filterNot { isUiChromeLine(it) }
        if (filtered.isEmpty()) return emptyList()

        val lastUserBoundary = filtered.indexOfLast { isLikelyUserBoundary(it) }
        val afterBoundary = if (lastUserBoundary >= 0 && lastUserBoundary + 1 < filtered.size) {
            filtered.drop(lastUserBoundary + 1)
        } else {
            filtered
        }

        val cleaned = afterBoundary
            .filterNot { isLikelyInputLine(it) }
            .filterNot { isLikelyButtonOnlyLine(it) }

        return when {
            cleaned.size <= 28 -> cleaned
            else -> cleaned.takeLast(28)
        }
    }

    private fun hasResponseEndControlsBelow(lines: List<UiLine>, sentCommandBottomY: Int): Boolean {
        val scoped = if (sentCommandBottomY > 0) {
            lines.filter { it.top >= sentCommandBottomY - COMMAND_Y_TOLERANCE_PX }
        } else {
            lines
        }
        val normalized = scoped.map { it.text.normalizeForCompare() }
        val hasPositiveFeedback = normalized.any {
            it == "good response" ||
                it.contains("good response") ||
                it.contains("thumbs up") ||
                it.contains("like")
        }
        val hasNegativeFeedback = normalized.any {
            it == "bad response" ||
                it.contains("bad response") ||
                it.contains("thumbs down") ||
                it.contains("dislike")
        }
        val hasSourceOrCopyControl = normalized.any {
            it == "copy" ||
                it.contains("copy") ||
                it.contains("sources") ||
                it.contains("source") ||
                it.contains("citations") ||
                it.contains("read aloud") ||
                it.contains("share")
        }
        return (hasPositiveFeedback && hasNegativeFeedback) ||
            ((hasPositiveFeedback || hasNegativeFeedback) && hasSourceOrCopyControl)
    }

    private fun hasGeneratingControl(lines: List<String>): Boolean {
        val normalized = lines.map { it.normalizeForCompare() }
        return normalized.any {
            it == "stop" ||
                it == "stop generating" ||
                it == "stop response" ||
                it.contains("stop generating") ||
                it.contains("stop response") ||
                it.contains("interrupt") ||
                it.contains("generating")
        }
    }

    private fun isUiChromeLine(line: String): Boolean {
        val normalized = line.lowercase()
        val exact = setOf(
            "chatgpt",
            "new chat",
            "search",
            "library",
            "explore gpts",
            "upgrade plan",
            "settings",
            "send",
            "stop",
            "regenerate",
            "copy",
            "share",
            "read aloud",
            "good response",
            "bad response"
        )
        if (normalized in exact) return true
        return normalized.startsWith("chatgpt can make mistakes") ||
            normalized.startsWith("message chatgpt") ||
            normalized.startsWith("ask anything") ||
            normalized.contains("search chat history")
    }

    private fun isLikelyUserBoundary(line: String): Boolean {
        val normalized = line.lowercase()
        return normalized == "you" ||
            normalized.startsWith("you said") ||
            normalized.startsWith("user")
    }

    private fun isLikelyInputLine(line: String): Boolean {
        val normalized = line.lowercase()
        return normalized == "message chatgpt" ||
            normalized == "ask anything" ||
            normalized.startsWith("message ")
    }

    private fun isLikelyButtonOnlyLine(line: String): Boolean {
        val normalized = line.lowercase()
        if (line.length > 40) return false
        val buttonWords = listOf("send", "copy", "share", "retry", "continue", "stop", "voice", "attach", "read aloud", "good response", "bad response")
        return buttonWords.any { normalized == it || normalized.startsWith("$it ") }
    }

    private fun String.normalizeForCompare(): String =
        lowercase().replace(Regex("\\s+"), " ").trim()

    private fun List<UiLine>.distinctConsecutiveByText(): List<UiLine> {
        val result = mutableListOf<UiLine>()
        for (item in this) {
            if (result.lastOrNull()?.text != item.text) result += item
        }
        return result
    }

    private const val COMMAND_Y_TOLERANCE_PX = 8
}
