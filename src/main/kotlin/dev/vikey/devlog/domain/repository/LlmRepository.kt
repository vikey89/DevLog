package dev.vikey.devlog.domain.repository

/** Sends prompts to a Large Language Model and returns generated text. */
interface LlmRepository {
    /**
     * Generates a narrative from the given [prompt].
     * Returns [Result.success] with the text or [Result.failure] on error.
     */
    suspend fun generate(prompt: String): Result<String>
}
