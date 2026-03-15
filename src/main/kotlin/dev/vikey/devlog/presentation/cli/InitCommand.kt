package dev.vikey.devlog.presentation.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import dev.vikey.devlog.data.ConfigLoader
import dev.vikey.devlog.domain.model.AppConfig
import dev.vikey.devlog.domain.model.Provider
import dev.vikey.devlog.domain.validator.ConfigValidator
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class InitCommand : CliktCommand(name = "init"), KoinComponent {
    override fun help(context: Context): String = "Initialize devlog configuration."

    override fun run() {
        echo("Welcome to DevLog! Let's set up your configuration.\n")
        val config = promptConfig() ?: return
        runBlocking { get<ConfigLoader>().save(config) }
        echo("\nConfiguration saved to ~/.devlog/config.yml")
        echo("Run 'devlog today' to generate your first report!")
    }

    private fun promptConfig(): AppConfig? {
        val workspace = promptWorkspace() ?: return null
        val options = Provider.entries.joinToString(", ") { it.id }
        val provider = promptWithDefault("LLM provider ($options)", Provider.ANTHROPIC.id)
        val model = promptWithDefault("Model", defaultModel(provider))
        val apiKeyEnv = promptApiKeyEnv(defaultApiKeyEnv(provider))
        val language = promptWithDefault("Language", "english")
        echo("Author (git author filter, leave empty for all):")
        val author = readlnOrNull()?.trim()?.ifBlank { null }
        return AppConfig(
            workspaces = listOf(workspace),
            provider = provider,
            model = model,
            apiKeyEnv = apiKeyEnv,
            language = language,
            author = author,
        )
    }

    private fun promptWorkspace(): String? {
        echo("Workspace path (directory containing git repos):")
        val value = readlnOrNull()?.trim()
        if (value.isNullOrBlank()) {
            echo("This field is required.", err = true)
            return null
        }
        return value
    }

    private fun promptWithDefault(label: String, default: String): String {
        echo("$label [$default]:")
        return readlnOrNull()?.trim()?.ifBlank { default } ?: default
    }

    private fun promptApiKeyEnv(default: String): String {
        val validator = get<ConfigValidator>()
        while (true) {
            val value = promptWithDefault("API key environment variable", default)
            if (validator.isValidEnvVarName(value)) return value
            echo(
                "That looks like an API key, not a variable name. " +
                    "Enter the env variable name (e.g. ANTHROPIC_API_KEY).",
                err = true,
            )
        }
    }

    private fun defaultModel(provider: String): String = when (Provider.fromId(provider)) {
        Provider.ANTHROPIC -> "claude-haiku-4-5-20251001"
        Provider.OPENAI -> "gpt-4o-mini"
        Provider.GEMINI -> "gemini-2.0-flash"
        else -> provider
    }

    private fun defaultApiKeyEnv(provider: String): String = when (Provider.fromId(provider)) {
        Provider.ANTHROPIC -> "ANTHROPIC_API_KEY"
        Provider.OPENAI -> "OPENAI_API_KEY"
        Provider.GEMINI -> "GEMINI_API_KEY"
        else -> "API_KEY"
    }
}
