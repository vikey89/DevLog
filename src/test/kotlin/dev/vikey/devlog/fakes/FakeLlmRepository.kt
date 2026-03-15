package dev.vikey.devlog.fakes

import dev.vikey.devlog.domain.repository.LlmRepository

class FakeLlmRepository(
    private val response: Result<String> = Result.success("Test narrative"),
) : LlmRepository {
    var callCount: Int = 0
        private set
    var lastPrompt: String? = null
        private set

    override suspend fun generate(prompt: String): Result<String> {
        callCount++
        lastPrompt = prompt
        return response
    }
}
