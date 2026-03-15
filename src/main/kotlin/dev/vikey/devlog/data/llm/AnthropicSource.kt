package dev.vikey.devlog.data.llm

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class AnthropicSource(
    private val client: HttpClient,
    private val apiKey: Lazy<String>,
    private val model: String,
) : LlmSource {

    override suspend fun generate(prompt: String, system: String): Result<String> = runCatching {
        val response: AnthropicResponse = client.post(API_URL) {
            headers {
                append("x-api-key", apiKey.value)
                append("anthropic-version", API_VERSION)
            }
            setBody(
                AnthropicRequest(
                    model = model,
                    maxTokens = MAX_TOKENS,
                    system = system,
                    messages = listOf(Message(role = "user", content = prompt)),
                )
            )
        }.body()

        response.content.first().text
    }

    companion object {
        private const val API_URL = "https://api.anthropic.com/v1/messages"
        private const val API_VERSION = "2023-06-01"
        private const val MAX_TOKENS = 4096
    }
}

@Serializable
internal data class Message(val role: String, val content: String)

@Serializable
internal data class AnthropicRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val system: String,
    val messages: List<Message>,
)

@Serializable
internal data class ContentBlock(val type: String = "", val text: String = "")

@Serializable
internal data class AnthropicResponse(val content: List<ContentBlock>)
