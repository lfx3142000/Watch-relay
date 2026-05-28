package com.example.chatgptwatchrelay.notifications

import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.chatgptwatchrelay.accessibility.ChatGptCommandSender

class ReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reply = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence(NotificationHelper.KEY_REPLY_TEXT)
            ?.toString()
            ?.trim()

        if (reply.isNullOrBlank()) {
            Toast.makeText(context, "No reply text received", Toast.LENGTH_SHORT).show()
            return
        }

        val limitedReply = appendWatchLimit(reply)
        NotificationHelper.dismissResponseNotification(context)
        if (DuplicateActionGuard.shouldIgnore("reply", limitedReply)) {
            Toast.makeText(context, "Duplicate reply ignored", Toast.LENGTH_SHORT).show()
            return
        }
        ChatGptCommandSender.queueCommand(context, limitedReply, "reply")
        Toast.makeText(context, "Sending reply to ChatGPT", Toast.LENGTH_SHORT).show()
    }

    private fun appendWatchLimit(reply: String): String {
        val normalized = reply.lowercase()
        val alreadyLimits = normalized.contains("limit") &&
            (normalized.contains("character") || normalized.contains("word") || normalized.contains("short"))
        if (alreadyLimits) return reply
        return "$reply\n\nPlease keep the response under 180 characters so it fits on my watch."
    }
}
