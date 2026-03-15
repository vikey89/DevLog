package dev.vikey.devlog.data.llm

import dev.vikey.devlog.domain.model.AppConfig
import dev.vikey.devlog.domain.model.Provider
import dev.vikey.devlog.domain.validator.ConfigValidator
import io.ktor.client.HttpClient

class ProviderRegistry(
    private val client: HttpClient,
    private val configValidator: ConfigValidator,
) {
    fun getSource(config: AppConfig): LlmSource {
        val apiKey = resolveApiKey(config)
        val provider = Provider.fromId(config.provider)
            ?: error(
                "Unknown LLM provider: '${config.provider}'. " +
                    "Supported: ${Provider.entries.joinToString { it.id }}"
            )

        return when (provider) {
            Provider.ANTHROPIC -> AnthropicSource(client, apiKey, config.model)
            Provider.OPENAI -> OpenAISource(client, apiKey, config.model)
            Provider.GEMINI -> GeminiSource(client, apiKey, config.model)
        }
    }

    private fun resolveApiKey(config: AppConfig): Lazy<String> {
        val envName = config.apiKeyEnv
        require(configValidator.isValidEnvVarName(envName)) {
            "apiKeyEnv must be an environment variable name (e.g. ANTHROPIC_API_KEY), " +
                "not the key itself. Fix your ~/.devlog/config.yml, " +
                "then: export ANTHROPIC_API_KEY=your-key"
        }
        return lazy {
            System.getenv(envName)
                ?: error("API key not found. Set the environment variable '$envName'.")
        }
    }
}
