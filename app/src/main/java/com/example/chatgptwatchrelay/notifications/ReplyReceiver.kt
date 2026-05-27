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

        NotificationHelper.dismissResponseNotification(context)
        ChatGptCommandSender.queueCommand(context, reply, "reply")
        Toast.makeText(context, "Sending reply to ChatGPT", Toast.LENGTH_SHORT).show()
    }
}
