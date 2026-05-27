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
    const val ACTION_CHECK_RUN = "com.example.chatgptwatchrelay.action.CHECK_RUN"
    const val ACTION_SEND_LINK = "com.example.chatgptwatchrelay.action.SEND_LINK"
    const val ACTION_OPEN = "com.example.chatgptwatchrelay.action.OPEN"
    const val ACTION_REPLY = "com.example.chatgptwatchrelay.action.REPLY"
    const val ACTION_DISMISS = "com.example.chatgptwatchrelay.action.DISMISS"
    const val ACTION_STOP_MONITORING = "com.example.chatgptwatchrelay.action.STOP_MONITORING"

    /**
     * Keep these prompts intentionally short and one-line.
     * Longer/multi-line prompts can expand the ChatGPT compose box and make
     * the Accessibility sender miss the Send button.
     */
    val defaultCommands = listOf(
        RelayCommand(ACTION_CONTINUE, "Continue", "Continue"),
        RelayCommand(ACTION_STATUS, "Status", "Status"),
        RelayCommand(ACTION_FINISHED, "Did you finish?", "Did you finish?"),
        RelayCommand(ACTION_SEND_LINK, "Send link", "Send link")
    ).also { commands ->
        require(commands.all { command -> command.prompt.isShortOneLine() }) {
            "Preset watch commands must be short one-line prompts."
        }
    }

    fun promptForAction(action: String?): String? = defaultCommands.firstOrNull { it.action == action }?.prompt

    private fun String.isShortOneLine(): Boolean =
        isNotBlank() && length <= 40 && !contains('\n') && !contains('\r')
}
