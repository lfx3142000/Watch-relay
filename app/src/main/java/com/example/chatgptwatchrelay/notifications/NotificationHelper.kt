package com.example.chatgptwatchrelay.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.chatgptwatchrelay.relay.CommandRepository
import com.example.chatgptwatchrelay.relay.RelayState

object NotificationHelper {
    const val CHANNEL_ID = "chatgpt_relay"
    const val RESPONSE_NOTIFICATION_ID = 1001
    const val COMMAND_STATUS_NOTIFICATION_ID = 1002
    const val KEY_REPLY_TEXT = "reply_text"
    private const val WATCH_TEXT_LIMIT = 180

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ChatGPT Relay",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Relays completed ChatGPT responses to your watch."
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    fun showResponseNotification(context: Context) {
        ensureChannel(context)
        val total = RelayState.chunks.size.coerceAtLeast(1)
        val index = RelayState.currentChunkIndex + 1
        val baseText = RelayState.currentChunk().ifBlank { "No response captured yet." }
        val fullText = if (index >= total) "$baseText\n\nEnd of response." else baseText
        val watchText = fullText.toWatchSnippet()

        val builder = android.app.Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("ChatGPT done — $index/$total")
            .setContentText(watchText)
            .setAutoCancel(true)
            .setOngoing(false)
            .setLocalOnly(false)
            .setVisibility(android.app.Notification.VISIBILITY_PUBLIC)
            .setCategory(android.app.Notification.CATEGORY_MESSAGE)
            .setDeleteIntent(pendingIntent(context, CommandRepository.ACTION_DISMISS))
            .addAction(action(context, CommandRepository.ACTION_MORE, "More"))
            .addAction(action(context, CommandRepository.ACTION_CONTINUE, "Continue"))
            .addAction(action(context, CommandRepository.ACTION_STATUS, "Status"))
            .addAction(action(context, CommandRepository.ACTION_STOP_MONITORING, "Stop"))
            .addAction(action(context, CommandRepository.ACTION_TEST_MESSAGE, "Test"))
            .addAction(action(context, CommandRepository.ACTION_SHORTER, "Shorter"))
            .addAction(action(context, CommandRepository.ACTION_CHECK_RUN, "Check"))
            .addAction(action(context, CommandRepository.ACTION_SEND_LINK, "Link"))

        context.getSystemService(NotificationManager::class.java)
            .notify(RESPONSE_NOTIFICATION_ID, builder.build())
    }

    fun dismissResponseNotification(context: Context) {
        context.getSystemService(NotificationManager::class.java)
            .cancel(RESPONSE_NOTIFICATION_ID)
    }

    fun showCommandFailureNotification(context: Context, reason: String) {
        ensureChannel(context)
        val text = "$reason The command was copied to clipboard as a fallback."
        val builder = android.app.Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("ChatGPT command not sent")
            .setContentText(text.take(120))
            .setStyle(android.app.Notification.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .addAction(action(context, CommandRepository.ACTION_OPEN, "Open ChatGPT"))

        context.getSystemService(NotificationManager::class.java)
            .notify(COMMAND_STATUS_NOTIFICATION_ID, builder.build())
    }

    private fun action(context: Context, action: String, label: String): android.app.Notification.Action {
        return android.app.Notification.Action.Builder(android.R.drawable.ic_menu_send, label, pendingIntent(context, action)).build()
    }

    private fun pendingIntent(context: Context, action: String): PendingIntent {
        val intent = Intent(context, CommandReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun replyAction(context: Context): android.app.Notification.Action {
        val remoteInput = RemoteInput.Builder(KEY_REPLY_TEXT)
            .setLabel("Reply to ChatGPT")
            .build()
        val intent = Intent(context, ReplyReceiver::class.java).setAction(CommandRepository.ACTION_REPLY)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            CommandRepository.ACTION_REPLY.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        return android.app.Notification.Action.Builder(android.R.drawable.ic_btn_speak_now, "Reply", pendingIntent)
            .addRemoteInput(remoteInput)
            .build()
    }

    private fun String.toWatchSnippet(): String {
        val oneLine = replace(Regex("\\s+"), " ").trim()
        if (oneLine.length <= WATCH_TEXT_LIMIT) return oneLine
        return oneLine.take(WATCH_TEXT_LIMIT - 1).trimEnd() + "…"
    }
}
