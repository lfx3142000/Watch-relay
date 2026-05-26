package com.example.chatgptwatchrelay.launch

import android.content.Context
import android.content.Intent
import android.net.Uri

object ChatGptLauncher {
    private const val CHATGPT_PACKAGE = "com.openai.chatgpt"
    private const val CHATGPT_WEB = "https://chatgpt.com/"

    fun open(context: Context) {
        val packageManager = context.packageManager

        val launchIntent = packageManager.getLaunchIntentForPackage(CHATGPT_PACKAGE)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            context.startActivity(launchIntent)
            return
        }

        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(CHATGPT_WEB)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            setPackage(CHATGPT_PACKAGE)
        }
        if (webIntent.resolveActivity(packageManager) != null) {
            context.startActivity(webIntent)
            return
        }

        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(CHATGPT_WEB)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(fallbackIntent)
    }
}
