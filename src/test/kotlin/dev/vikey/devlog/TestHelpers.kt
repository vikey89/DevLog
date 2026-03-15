package dev.vikey.devlog

import dev.vikey.devlog.domain.model.AppConfig
import dev.vikey.devlog.domain.model.CommitInfo
import dev.vikey.devlog.domain.model.Provider
import dev.vikey.devlog.domain.model.RepoActivity

fun makeTestConfig(
    workspaces: List<String> = listOf("/tmp/test-workspace"),
    provider: String = Provider.ANTHROPIC.id,
    model: String = "claude-haiku-4-5-20251001",
    apiKeyEnv: String = "ANTHROPIC_API_KEY",
    language: String = "en",
    author: String? = null,
): AppConfig = AppConfig(
    workspaces = workspaces,
    provider = provider,
    model = model,
    apiKeyEnv = apiKeyEnv,
    language = language,
    author = author,
)

fun makeCommit(
    hash: String = "abc1234",
    message: String = "feat: add feature",
    timestamp: String = "2026-03-05T10:00:00",
    filesChanged: Int = 3,
    insertions: Int = 50,
    deletions: Int = 10,
): CommitInfo = CommitInfo(
    hash = hash,
    message = message,
    timestamp = timestamp,
    filesChanged = filesChanged,
    insertions = insertions,
    deletions = deletions,
)

fun makeActivity(
    name: String = "test-repo",
    commitsCount: Int = 3,
): RepoActivity {
    val commits = (1..commitsCount).map { i ->
        makeCommit(
            hash = "hash$i",
            message = "commit message $i",
            filesChanged = i,
            insertions = i * 10,
            deletions = i * 2,
        )
    }
    return RepoActivity(
        name = name,
        path = "/tmp/$name",
        branch = "main",
        commits = commits,
        totalInsertions = commits.sumOf { it.insertions },
        totalDeletions = commits.sumOf { it.deletions },
        totalFilesChanged = commits.sumOf { it.filesChanged },
    )
}
