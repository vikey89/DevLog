package dev.vikey.devlog.data.cache

import dev.vikey.devlog.data.dto.DevlogReportDto
import dev.vikey.devlog.data.dto.toDto
import dev.vikey.devlog.domain.model.DevlogReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class FileCacheSource {

    private val cacheDir: Path = Path.of(System.getProperty("user.home"), ".devlog", "cache")

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun get(key: String): DevlogReport? = withContext(Dispatchers.IO) {
        val file = cacheDir.resolve("$key.json")
        if (!file.exists()) return@withContext null
        try {
            json.decodeFromString<DevlogReportDto>(file.readText()).toDomain()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun save(key: String, report: DevlogReport): Unit = withContext(Dispatchers.IO) {
        cacheDir.createDirectories()
        val file = cacheDir.resolve("$key.json")
        file.writeText(json.encodeToString(DevlogReportDto.serializer(), report.toDto()))
    }
}
