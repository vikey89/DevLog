package dev.vikey.devlog.domain.repository

import dev.vikey.devlog.domain.model.DevlogReport

/** Persists and retrieves generated reports to avoid redundant LLM calls. */
interface CacheRepository {
    /** Returns a cached report for the given [key], or `null` if not found. */
    suspend fun get(key: String): DevlogReport?
    /** Saves a [report] under the given [key]. */
    suspend fun save(key: String, report: DevlogReport)
}
