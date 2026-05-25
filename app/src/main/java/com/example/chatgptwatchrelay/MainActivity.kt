package com.example.chatgptwatchrelay

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.example.chatgptwatchrelay.launch.ChatGptLauncher
import com.example.chatgptwatchrelay.notifications.NotificationHelper
import com.example.chatgptwatchrelay.relay.RelayState

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildLayout())
    }

    private fun buildLayout(): ScrollView {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        container.addView(TextView(this).apply {
            text = "ChatGPT Watch Relay"
            textSize = 24f
        })

        container.addView(TextView(this).apply {
            text = "Automatic MVP target: detect ChatGPT completion, notify watch, and relay watch replies back to ChatGPT."
            textSize = 16f
            setPadding(0, 20, 0, 20)
        })

        container.addView(button("Allow Notifications") { requestNotificationPermission() })
        container.addView(button("Enable Accessibility") { openAccessibilitySettings() })
        container.addView(button("Open ChatGPT") { ChatGptLauncher.open(this) })
        container.addView(button("Start Monitoring") {
            RelayState.monitoringEnabled = true
            Toast.makeText(this, "Monitoring enabled", Toast.LENGTH_SHORT).show()
        })
        container.addView(button("Stop Monitoring") {
            RelayState.monitoringEnabled = false
            Toast.makeText(this, "Monitoring stopped", Toast.LENGTH_SHORT).show()
        })
        container.addView(button("Send Test Watch Notification") {
            RelayState.setResponse("This is a test ChatGPT response relayed to your watch. Use More, Continue, Summarize, or Reply from the notification actions.")
            NotificationHelper.showResponseNotification(this)
        })

        container.addView(TextView(this).apply {
            text = "Current implementation status:\n- Notification and watch actions are scaffolded.\n- Accessibility service is scaffolded.\n- Full ChatGPT extraction and auto-send are next."
            textSize = 14f
            setPadding(0, 24, 0, 0)
        })

        return ScrollView(this).apply { addView(container) }
    }

    private fun button(label: String, onClick: () -> Unit): Button = Button(this).apply {
        text = label
        setOnClickListener { onClick() }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
        } else {
            Toast.makeText(this, "Notification permission is already available on this Android version", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }
}
