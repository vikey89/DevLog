package dev.vikey.devlog.domain.model

data class AppConfig(
    val workspaces: List<String>,
    val provider: String,
    val model: String,
    val apiKeyEnv: String,
    val language: String,
    val author: String? = null,
) {
    val workspaceHash: String
        get() = workspaces.sorted().joinToString(",").hashCode().toUInt().toString(radix = 16)
}
