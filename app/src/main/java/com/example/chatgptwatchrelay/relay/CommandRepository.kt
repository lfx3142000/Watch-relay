package com.example.chatgptwatchrelay.relay

data class RelayCommand(
    val action: String,
    val label: String,
    val prompt: String
)

object CommandRepository {
    const val ACTION_MORE = "com.example.chatgptwatchrelay.action.MORE"
    const val ACTION_CONTINUE = "com.example.chatgptwatchrelay.action.CONTINUE"
    const val ACTION_STATUS = "com.example.chatgptwatchrelay.action.STATUS"
    const val ACTION_FINISHED = "com.example.chatgptwatchrelay.action.FINISHED"
    const val ACTION_SUMMARIZE = "com.example.chatgptwatchrelay.action.SUMMARIZE"
    const val ACTION_SHORTER = "com.example.chatgptwatchrelay.action.SHORTER"
    const val ACTION_OPEN = "com.example.chatgptwatchrelay.action.OPEN"
    const val ACTION_REPLY = "com.example.chatgptwatchrelay.action.REPLY"
    const val ACTION_DISMISS = "com.example.chatgptwatchrelay.action.DISMISS"
    const val ACTION_STOP_MONITORING = "com.example.chatgptwatchrelay.action.STOP_MONITORING"

    val defaultCommands = listOf(
        RelayCommand(ACTION_CONTINUE, "Continue", "Continue from where you left off. Keep going with the next useful steps."),
        RelayCommand(ACTION_STATUS, "Status", "Give me a brief status update. What is done, what is currently happening, and what is next?"),
        RelayCommand(ACTION_FINISHED, "Done?", "Did you finish? Answer briefly and tell me what remains if anything is not done."),
        RelayCommand(ACTION_SHORTER, "Shorter", "Make that shorter and more direct."),
        RelayCommand(ACTION_SUMMARIZE, "Summarize", "Summarize your last response in 5 short bullets.")
    )

    fun promptForAction(action: String?): String? = defaultCommands.firstOrNull { it.action == action }?.prompt
}
