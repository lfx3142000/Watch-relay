package com.example.chatgptwatchrelay.launch

import android.content.Context
import android.content.Intent
import android.net.Uri

object ChatGptLauncher {
    private const val CHATGPT_PACKAGE = "com.openai.chatgpt"
    private const val CHATGPT_WEB = "https://chatgpt.com/"

    fun open(context: Context) {
        val packageIntent = context.packageManager.getLaunchIntentForPackage(CHATGPT_PACKAGE)
        val intent = packageIntent ?: Intent(Intent.ACTION_VIEW, Uri.parse(CHATGPT_WEB))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
