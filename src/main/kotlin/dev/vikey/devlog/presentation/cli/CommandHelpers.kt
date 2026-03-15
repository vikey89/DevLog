package dev.vikey.devlog.presentation.cli

import dev.vikey.devlog.domain.model.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

internal fun applyWorkspaceOverride(config: AppConfig, workspace: String?): AppConfig =
    workspace?.let { config.copy(workspaces = listOf(it)) } ?: config

internal fun handlePostActions(narrative: String?, copy: Boolean, outputPath: String?) {
    if (copy && narrative != null) {
        copyToClipboard(narrative)
    }
    outputPath?.let { path ->
        File(path).writeText(narrative ?: "")
    }
}

internal suspend fun <T> withSpinner(message: String, block: suspend () -> T): T = coroutineScope {
    val frames = listOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")
    val job = launch(Dispatchers.Default) {
        var i = 0
        while (isActive) {
            System.out.print("\r  ${frames[i % frames.size]} $message")
            System.out.flush()
            delay(SPINNER_DELAY_MS)
            i++
        }
    }
    try {
        block()
    } finally {
        job.cancel()
        System.out.print("\r${" ".repeat(message.length + SPINNER_PADDING)}\r")
        System.out.flush()
    }
}

private const val SPINNER_DELAY_MS = 80L
private const val SPINNER_PADDING = 6

private fun copyToClipboard(text: String) {
    val os = System.getProperty("os.name").lowercase()
    val command = when {
        "mac" in os -> listOf("pbcopy")
        "linux" in os -> listOf("xclip", "-selection", "clipboard")
        "windows" in os -> listOf("clip.exe")
        else -> return
    }
    runCatching {
        val process = ProcessBuilder(command).start()
        process.outputStream.use { it.write(text.toByteArray()) }
        process.waitFor()
    }
}
