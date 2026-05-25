package com.example.chatgptwatchrelay.relay

object RelayDiagnostics {
    var lastPackageName: String = "Unknown"
    var lastVisibleTextLength: Int = 0
    var lastVisibleTextLineCount: Int = 0
    var lastLikelyResponseLength: Int = 0
    var lastLikelyResponseLineCount: Int = 0
    var lastInputCandidateCount: Int = 0
    var lastSendCandidateCount: Int = 0
    var lastEventSummary: String = "No events yet"
    var lastCommandStatus: String = "No command queued"

    fun updateScreenSnapshot(packageName: String, visibleText: String) {
        lastPackageName = packageName.ifBlank { "Unknown" }
        lastVisibleTextLength = visibleText.length
        lastVisibleTextLineCount = visibleText.lines().count { it.isNotBlank() }
        lastEventSummary = "Saw ${lastVisibleTextLineCount} text lines from ${lastPackageName}"
    }

    fun updateLikelyResponse(responseText: String, responseLineCount: Int) {
        lastLikelyResponseLength = responseText.length
        lastLikelyResponseLineCount = responseLineCount
    }

    fun updateCommandCandidates(inputCandidates: Int, sendCandidates: Int) {
        lastInputCandidateCount = inputCandidates
        lastSendCandidateCount = sendCandidates
    }

    fun commandQueued(source: String) {
        lastCommandStatus = "Queued $source command"
    }

    fun commandSent() {
        lastCommandStatus = "Command sent"
    }

    fun commandFailed(reason: String) {
        lastCommandStatus = "Command failed: $reason"
    }

    fun summary(): String = buildString {
        appendLine("Package: $lastPackageName")
        appendLine("Visible text lines: $lastVisibleTextLineCount")
        appendLine("Visible text chars: $lastVisibleTextLength")
        appendLine("Likely response lines: $lastLikelyResponseLineCount")
        appendLine("Likely response chars: $lastLikelyResponseLength")
        appendLine("Input candidates: $lastInputCandidateCount")
        appendLine("Send candidates: $lastSendCandidateCount")
        appendLine("Last event: $lastEventSummary")
        appendLine("Command: $lastCommandStatus")
    }
}
