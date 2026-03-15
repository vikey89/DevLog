package dev.vikey.devlog.data.llm

import dev.vikey.devlog.domain.model.AppConfig
import dev.vikey.devlog.domain.model.Provider
import dev.vikey.devlog.domain.validator.ConfigValidator
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlin.test.Test

class ProviderRegistryTest {

    private val registry = ProviderRegistry(HttpClient(CIO), ConfigValidator())

    private fun configWith(provider: String) = AppConfig(
        workspaces = listOf("/tmp/test"),
        provider = provider,
        model = "test-model",
        apiKeyEnv = "TEST_API_KEY",
        language = "en",
    )

    @Test
    fun `getSource returns AnthropicSource for anthropic provider`() {
        val source = registry.getSource(configWith(Provider.ANTHROPIC.id))
        source.shouldBeInstanceOf<AnthropicSource>()
    }

    @Test
    fun `getSource returns OpenAISource for openai provider`() {
        val source = registry.getSource(configWith(Provider.OPENAI.id))
        source.shouldBeInstanceOf<OpenAISource>()
    }

    @Test
    fun `getSource returns GeminiSource for gemini provider`() {
        val source = registry.getSource(configWith(Provider.GEMINI.id))
        source.shouldBeInstanceOf<GeminiSource>()
    }

    @Test
    fun `getSource throws for unknown provider`() {
        val error = shouldThrow<IllegalStateException> {
            registry.getSource(configWith("unknown"))
        }
        error.message shouldBe
            "Unknown LLM provider: 'unknown'. Supported: ${Provider.entries.joinToString { it.id }}"
    }

    @Test
    fun `getSource throws when apiKeyEnv contains a raw API key`() {
        val config = configWith(Provider.ANTHROPIC.id).copy(apiKeyEnv = "sk-ant-abc123xyz")
        val error = shouldThrow<IllegalArgumentException> {
            registry.getSource(config)
        }
        error.message shouldBe
            "apiKeyEnv must be an environment variable name (e.g. ANTHROPIC_API_KEY), " +
            "not the key itself. Fix your ~/.devlog/config.yml, " +
            "then: export ANTHROPIC_API_KEY=your-key"
    }
}
