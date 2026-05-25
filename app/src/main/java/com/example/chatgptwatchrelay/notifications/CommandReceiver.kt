package com.example.chatgptwatchrelay.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.chatgptwatchrelay.accessibility.ChatGptCommandSender
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
                    ChatGptCommandSender.queueCommand(context, prompt, "command")
                    Toast.makeText(context, "Sending command to ChatGPT", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
