package dev.vikey.devlog.fakes

import dev.vikey.devlog.domain.model.DevlogReport
import dev.vikey.devlog.domain.repository.CacheRepository

class FakeCacheRepository : CacheRepository {
    private val cache = mutableMapOf<String, DevlogReport>()
    var saveCount: Int = 0
        private set
    var lastSavedKey: String? = null
        private set

    fun setCached(key: String, report: DevlogReport) {
        cache[key] = report
    }

    override suspend fun get(key: String): DevlogReport? = cache[key]

    override suspend fun save(key: String, report: DevlogReport) {
        saveCount++
        lastSavedKey = key
        cache[key] = report
    }
}
