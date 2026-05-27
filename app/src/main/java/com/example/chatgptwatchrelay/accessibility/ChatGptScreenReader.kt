package com.example.chatgptwatchrelay.accessibility

import android.view.accessibility.AccessibilityNodeInfo

object ChatGptScreenReader {
    data class ScreenSnapshot(
        val allVisibleText: String,
        val likelyLatestResponse: String,
        val visibleLineCount: Int,
        val responseLineCount: Int,
        val hasResponseEndControls: Boolean
    )

    fun read(root: AccessibilityNodeInfo?): ScreenSnapshot {
        val lines = collectLines(root)
        val allText = lines.joinToString("\n")
        val responseLines = extractLikelyResponseLines(lines)
        return ScreenSnapshot(
            allVisibleText = allText,
            likelyLatestResponse = responseLines.joinToString("\n").trim(),
            visibleLineCount = lines.size,
            responseLineCount = responseLines.size,
            hasResponseEndControls = hasResponseEndControls(lines)
        )
    }

    private fun collectLines(root: AccessibilityNodeInfo?): List<String> {
        val lines = mutableListOf<String>()
        fun visit(node: AccessibilityNodeInfo?) {
            if (node == null) return
            if (node.isVisibleToUser) {
                val text = node.text?.toString()?.trim().orEmpty()
                val desc = node.contentDescription?.toString()?.trim().orEmpty()
                if (text.isNotBlank()) lines += text
                if (desc.isNotBlank() && desc != text) lines += desc
            }
            for (i in 0 until node.childCount) visit(node.getChild(i))
        }
        visit(root)
        return lines
            .flatMap { it.lines() }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctConsecutive()
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

    private fun hasResponseEndControls(lines: List<String>): Boolean {
        val normalized = lines.map { it.lowercase().replace(Regex("\\s+"), " ").trim() }
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

    private fun List<String>.distinctConsecutive(): List<String> {
        val result = mutableListOf<String>()
        for (item in this) {
            if (result.lastOrNull() != item) result += item
        }
        return result
    }
}
