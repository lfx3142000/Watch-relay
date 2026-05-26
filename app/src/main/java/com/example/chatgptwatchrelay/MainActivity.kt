package com.example.chatgptwatchrelay

import android.Manifest
import android.app.Activity
import android.content.Intent
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
import com.example.chatgptwatchrelay.relay.RelayDiagnostics
import com.example.chatgptwatchrelay.relay.RelayState

class MainActivity : Activity() {
    private lateinit var diagnosticsText: TextView

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
            text = "Automatic MVP target: detect each completed ChatGPT response, notify watch, and relay watch replies back to ChatGPT."
            textSize = 16f
            setPadding(0, 20, 0, 20)
        })

        container.addView(button("Allow Notifications") { requestNotificationPermission() })
        container.addView(button("Enable Accessibility") { openAccessibilitySettings() })
        container.addView(button("Open ChatGPT") { ChatGptLauncher.open(this) })
        container.addView(button("Start Monitoring") {
            RelayState.startMonitoring()
            Toast.makeText(this, "Monitoring enabled. Will notify once per new response.", Toast.LENGTH_SHORT).show()
            refreshDiagnostics()
        })
        container.addView(button("Stop Monitoring") {
            RelayState.stopNotifications()
            Toast.makeText(this, "Relay notifications stopped", Toast.LENGTH_SHORT).show()
            refreshDiagnostics()
        })
        container.addView(button("Send Test Watch Notification") {
            RelayState.setResponse("This is a test ChatGPT response relayed to your watch. Use Continue, Status, Done?, Shorter, Stop alerts, More, or Reply from the notification actions.", markNotified = false)
            NotificationHelper.showResponseNotification(this)
            refreshDiagnostics()
        })
        container.addView(button("Refresh Diagnostics") { refreshDiagnostics() })

        diagnosticsText = TextView(this).apply {
            text = diagnosticsSummary()
            textSize = 14f
            setPadding(0, 24, 0, 0)
        }
        container.addView(diagnosticsText)

        return ScrollView(this).apply { addView(container) }
    }

    private fun button(label: String, onClick: () -> Unit): Button = Button(this).apply {
        text = label
        setOnClickListener { onClick() }
    }

    private fun refreshDiagnostics() {
        if (::diagnosticsText.isInitialized) {
            diagnosticsText.text = diagnosticsSummary()
        }
    }

    private fun diagnosticsSummary(): String = buildString {
        appendLine("Status:")
        appendLine("Monitoring: ${if (RelayState.monitoringEnabled) "Active" else "Stopped"}")
        appendLine("Notify once per response: ${if (RelayState.notifyOnceEnabled) "On" else "Off"}")
        appendLine("Last response notified: ${if (RelayState.lastNotifiedFingerprint != 0) "Yes" else "No"}")
        appendLine("Captured chunks: ${RelayState.chunks.size}")
        appendLine()
        appendLine("Diagnostics:")
        append(RelayDiagnostics.summary())
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
