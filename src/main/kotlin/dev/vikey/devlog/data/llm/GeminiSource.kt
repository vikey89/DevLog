package dev.vikey.devlog.data.llm

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable

class GeminiSource(
    private val client: HttpClient,
    private val apiKey: Lazy<String>,
    private val model: String,
) : LlmSource {

    override suspend fun generate(prompt: String, system: String): Result<String> = runCatching {
        val response: GeminiResponse = client.post(
            "$BASE_URL/models/$model:generateContent?key=${apiKey.value}"
        ) {
            setBody(
                GeminiRequest(
                    contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = system))),
                )
            )
        }.body()

        response.candidates.first().content.parts.first().text
    }

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
    }
}

@Serializable
internal data class GeminiPart(val text: String)

@Serializable
internal data class GeminiContent(val parts: List<GeminiPart>)

@Serializable
internal data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent,
)

@Serializable
internal data class Candidate(val content: GeminiContent)

@Serializable
internal data class GeminiResponse(val candidates: List<Candidate>)
