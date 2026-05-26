package com.example.chatgptwatchrelay.notifications

import android.app.NotificationManager
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
            CommandRepository.ACTION_DISMISS -> {
                context.getSystemService(NotificationManager::class.java)
                    .cancel(NotificationHelper.RESPONSE_NOTIFICATION_ID)
            }
            CommandRepository.ACTION_STOP_MONITORING -> {
                RelayState.stopNotifications()
                context.getSystemService(NotificationManager::class.java)
                    .cancel(NotificationHelper.RESPONSE_NOTIFICATION_ID)
                Toast.makeText(context, "Relay notifications stopped", Toast.LENGTH_SHORT).show()
            }
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
