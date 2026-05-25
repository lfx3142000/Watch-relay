package com.example.chatgptwatchrelay.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.chatgptwatchrelay.launch.ChatGptLauncher
import com.example.chatgptwatchrelay.relay.CommandRepository
import com.example.chatgptwatchrelay.relay.RelayState

class CommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            CommandRepository.ACTION_MORE -> {
                RelayState.advanceChunk()
                NotificationHelper.showResponseNotification(context)
            }
            CommandRepository.ACTION_OPEN -> ChatGptLauncher.open(context)
            else -> {
                val prompt = CommandRepository.promptForAction(intent.action)
                if (prompt != null) {
                    copyToClipboard(context, prompt)
                    Toast.makeText(context, "Command copied for ChatGPT", Toast.LENGTH_SHORT).show()
                    ChatGptLauncher.open(context)
                }
            }
        }
    }

    private fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboard.setPrimaryClip(android.content.ClipData.newPlainText("ChatGPT command", text))
    }
}
