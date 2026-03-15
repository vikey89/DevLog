package dev.vikey.devlog.domain.model

data class CommitInfo(
    val hash: String,
    val message: String,
    val timestamp: String,
    val filesChanged: Int,
    val insertions: Int,
    val deletions: Int,
)

data class RepoActivity(
    val name: String,
    val path: String,
    val branch: String,
    val commits: List<CommitInfo>,
    val totalInsertions: Int,
    val totalDeletions: Int,
    val totalFilesChanged: Int,
) {
    val isEmpty: Boolean get() = commits.isEmpty()
}
