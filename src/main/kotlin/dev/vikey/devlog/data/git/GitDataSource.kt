package dev.vikey.devlog.data.git

import dev.vikey.devlog.domain.model.RepoActivity

interface GitDataSource {
    suspend fun getReposActivity(
        workspaces: List<String>,
        author: String?,
        since: String,
        until: String,
    ): List<RepoActivity>
}
