package dev.vikey.devlog.data.llm

interface LlmSource {
    suspend fun generate(prompt: String, system: String): Result<String>
}
