package dev.vikey.devlog.data.repository

import dev.vikey.devlog.domain.prompt.SYSTEM_PROMPT
import dev.vikey.devlog.data.llm.LlmSource
import dev.vikey.devlog.domain.repository.LlmRepository

class LlmRepositoryImpl(
    private val source: LlmSource,
) : LlmRepository {
    override suspend fun generate(prompt: String): Result<String> =
        source.generate(prompt, SYSTEM_PROMPT)
}
