package dev.vikey.devlog.domain.model

data class DevlogReport(
    val activities: List<RepoActivity>,
    val narrative: String? = null,
    val error: String? = null,
    val generatedAt: String,
    val formatType: String,
    val dateRange: String = "",
) {
    val isEmpty: Boolean get() = activities.all { it.isEmpty }

    fun withNarrative(text: String): DevlogReport =
        copy(narrative = text)

    fun withError(message: String): DevlogReport =
        copy(error = message)
}
