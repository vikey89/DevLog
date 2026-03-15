package dev.vikey.devlog.data.dto

import dev.vikey.devlog.domain.model.CommitInfo
import dev.vikey.devlog.domain.model.DevlogReport
import dev.vikey.devlog.domain.model.RepoActivity
import kotlinx.serialization.Serializable

@Serializable
data class CommitInfoDto(
    val hash: String,
    val message: String,
    val timestamp: String,
    val filesChanged: Int,
    val insertions: Int,
    val deletions: Int,
) {
    fun toDomain(): CommitInfo = CommitInfo(
        hash = hash,
        message = message,
        timestamp = timestamp,
        filesChanged = filesChanged,
        insertions = insertions,
        deletions = deletions,
    )
}

@Serializable
data class RepoActivityDto(
    val name: String,
    val path: String,
    val branch: String,
    val commits: List<CommitInfoDto>,
    val totalInsertions: Int,
    val totalDeletions: Int,
    val totalFilesChanged: Int,
) {
    fun toDomain(): RepoActivity = RepoActivity(
        name = name,
        path = path,
        branch = branch,
        commits = commits.map { it.toDomain() },
        totalInsertions = totalInsertions,
        totalDeletions = totalDeletions,
        totalFilesChanged = totalFilesChanged,
    )
}

@Serializable
data class DevlogReportDto(
    val activities: List<RepoActivityDto>,
    val narrative: String? = null,
    val generatedAt: String,
    val formatType: String,
    val dateRange: String = "",
) {
    fun toDomain(): DevlogReport = DevlogReport(
        activities = activities.map { it.toDomain() },
        narrative = narrative,
        generatedAt = generatedAt,
        formatType = formatType,
        dateRange = dateRange,
    )
}

fun CommitInfo.toDto(): CommitInfoDto = CommitInfoDto(
    hash = hash,
    message = message,
    timestamp = timestamp,
    filesChanged = filesChanged,
    insertions = insertions,
    deletions = deletions,
)

fun RepoActivity.toDto(): RepoActivityDto = RepoActivityDto(
    name = name,
    path = path,
    branch = branch,
    commits = commits.map { it.toDto() },
    totalInsertions = totalInsertions,
    totalDeletions = totalDeletions,
    totalFilesChanged = totalFilesChanged,
)

fun DevlogReport.toDto(): DevlogReportDto = DevlogReportDto(
    activities = activities.map { it.toDto() },
    narrative = narrative,
    generatedAt = generatedAt,
    formatType = formatType,
    dateRange = dateRange,
)
