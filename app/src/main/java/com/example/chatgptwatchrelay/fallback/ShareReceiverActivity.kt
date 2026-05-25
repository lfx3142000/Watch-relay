package com.example.chatgptwatchrelay.fallback

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.example.chatgptwatchrelay.notifications.NotificationHelper
import com.example.chatgptwatchrelay.relay.RelayState

class ShareReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        if (sharedText.isNotBlank()) {
            RelayState.setResponse(sharedText)
            NotificationHelper.showResponseNotification(this)
        }
        finish()
    }
}
