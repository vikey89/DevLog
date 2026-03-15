package dev.vikey.devlog.data.repository

import dev.vikey.devlog.data.cache.FileCacheSource
import dev.vikey.devlog.domain.model.DevlogReport
import dev.vikey.devlog.domain.repository.CacheRepository

class CacheRepositoryImpl(private val source: FileCacheSource) : CacheRepository {

    override suspend fun get(key: String): DevlogReport? = source.get(key)

    override suspend fun save(key: String, report: DevlogReport): Unit = source.save(key, report)
}
