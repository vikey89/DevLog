package dev.vikey.devlog.data.dto

import dev.vikey.devlog.domain.model.AppConfig
import kotlinx.serialization.Serializable

@Serializable
data class AppConfigDto(
    val workspaces: List<String>,
    val provider: String,
    val model: String,
    val apiKeyEnv: String,
    val language: String,
    val author: String? = null,
) {
    fun toDomain(): AppConfig = AppConfig(
        workspaces = workspaces,
        provider = provider,
        model = model,
        apiKeyEnv = apiKeyEnv,
        language = language,
        author = author,
    )
}

fun AppConfig.toDto(): AppConfigDto = AppConfigDto(
    workspaces = workspaces,
    provider = provider,
    model = model,
    apiKeyEnv = apiKeyEnv,
    language = language,
    author = author,
)
