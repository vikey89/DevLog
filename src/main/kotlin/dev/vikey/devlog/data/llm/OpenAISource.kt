package dev.vikey.devlog.data.llm

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

class OpenAISource(
    private val client: HttpClient,
    private val apiKey: Lazy<String>,
    private val model: String,
    private val baseUrl: String = API_URL,
) : LlmSource {

    override suspend fun generate(prompt: String, system: String): Result<String> = runCatching {
        val response: OpenAIResponse = client.post(baseUrl) {
            headers {
                append("Authorization", "Bearer ${apiKey.value}")
            }
            setBody(
                OpenAIRequest(
                    model = model,
                    messages = listOf(
                        OpenAIMessage(role = "system", content = system),
                        OpenAIMessage(role = "user", content = prompt),
                    ),
                )
            )
        }.body()

        response.choices.first().message.content
    }

    companion object {
        private const val API_URL = "https://api.openai.com/v1/chat/completions"
    }
}

@Serializable
internal data class OpenAIMessage(val role: String, val content: String)

@Serializable
internal data class OpenAIRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
)

@Serializable
internal data class ChoiceMessage(val content: String)

@Serializable
internal data class Choice(val message: ChoiceMessage)

@Serializable
internal data class OpenAIResponse(val choices: List<Choice>)
