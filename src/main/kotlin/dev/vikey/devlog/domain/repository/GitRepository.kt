package dev.vikey.devlog.domain.repository

import dev.vikey.devlog.domain.model.RepoActivity

/** Retrieves git commit activity from local repositories. */
interface GitRepository {
    /**
     * Scans [workspaces] for git repos and returns activity
     * between [since] and [until], optionally filtered by [author].
     */
    suspend fun getActivity(
        workspaces: List<String>,
        author: String?,
        since: String,
        until: String,
    ): List<RepoActivity>
}
