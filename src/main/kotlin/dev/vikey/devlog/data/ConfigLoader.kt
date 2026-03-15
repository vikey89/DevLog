package dev.vikey.devlog.data

import com.charleskorn.kaml.Yaml
import dev.vikey.devlog.data.dto.AppConfigDto
import dev.vikey.devlog.data.dto.toDto
import dev.vikey.devlog.domain.model.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

class ConfigLoader(
    private val configPath: Path = Path.of(System.getProperty("user.home"), ".devlog", "config.yml"),
) {

    suspend fun load(): AppConfig = withContext(Dispatchers.IO) {
        if (!configPath.exists()) {
            error("Config not found. Run 'devlog init' first.")
        }
        Yaml.default.decodeFromString(AppConfigDto.serializer(), configPath.readText()).toDomain()
    }

    suspend fun save(config: AppConfig): Unit = withContext(Dispatchers.IO) {
        configPath.parent.createDirectories()
        configPath.writeText(Yaml.default.encodeToString(AppConfigDto.serializer(), config.toDto()))
    }
}
