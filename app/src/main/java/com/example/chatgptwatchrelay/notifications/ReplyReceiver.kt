package com.example.chatgptwatchrelay.notifications

import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.chatgptwatchrelay.launch.ChatGptLauncher

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

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("ChatGPT reply", reply))
        Toast.makeText(context, "Reply copied for ChatGPT", Toast.LENGTH_SHORT).show()
        ChatGptLauncher.open(context)
    }
}
