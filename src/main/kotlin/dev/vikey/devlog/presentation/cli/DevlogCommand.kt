package dev.vikey.devlog.presentation.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.versionOption
import dev.vikey.devlog.APP_VERSION

class DevlogCommand : CliktCommand(name = "devlog") {
    override fun help(context: Context): String = "AI-powered dev diary from your git history."

    init {
        versionOption(APP_VERSION)
    }

    override fun run() = Unit
}
