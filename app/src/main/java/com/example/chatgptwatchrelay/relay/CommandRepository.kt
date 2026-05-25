package com.example.chatgptwatchrelay.relay

data class RelayCommand(
    val action: String,
    val label: String,
    val prompt: String
)

object CommandRepository {
    const val ACTION_MORE = "com.example.chatgptwatchrelay.action.MORE"
    const val ACTION_CONTINUE = "com.example.chatgptwatchrelay.action.CONTINUE"
    const val ACTION_SUMMARIZE = "com.example.chatgptwatchrelay.action.SUMMARIZE"
    const val ACTION_SHORTER = "com.example.chatgptwatchrelay.action.SHORTER"
    const val ACTION_OPEN = "com.example.chatgptwatchrelay.action.OPEN"
    const val ACTION_REPLY = "com.example.chatgptwatchrelay.action.REPLY"

    val defaultCommands = listOf(
        RelayCommand(ACTION_CONTINUE, "Continue", "Continue from where you left off."),
        RelayCommand(ACTION_SUMMARIZE, "Summarize", "Summarize your last response in 5 short bullets."),
        RelayCommand(ACTION_SHORTER, "Shorter", "Make that shorter and more direct.")
    )

    fun promptForAction(action: String?): String? = defaultCommands.firstOrNull { it.action == action }?.prompt
}
