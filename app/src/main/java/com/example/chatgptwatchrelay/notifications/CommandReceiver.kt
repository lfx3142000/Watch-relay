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
            CommandRepository.ACTION_OPEN -> {
                NotificationHelper.dismissResponseNotification(context)
                ChatGptLauncher.open(context)
            }
            CommandRepository.ACTION_DISMISS -> {
                NotificationHelper.dismissResponseNotification(context)
            }
            CommandRepository.ACTION_STOP_MONITORING -> {
                RelayState.stopNotifications()
                NotificationHelper.dismissResponseNotification(context)
                Toast.makeText(context, "Relay notifications stopped", Toast.LENGTH_SHORT).show()
            }
            else -> {
                val prompt = CommandRepository.promptForAction(intent.action)
                if (prompt != null) {
                    NotificationHelper.dismissResponseNotification(context)
                    ChatGptCommandSender.queueCommand(context, prompt, "command")
                    Toast.makeText(context, "Sending command to ChatGPT", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
