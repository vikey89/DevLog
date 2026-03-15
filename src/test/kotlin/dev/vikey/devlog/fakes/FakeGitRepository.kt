package dev.vikey.devlog.fakes

import dev.vikey.devlog.domain.model.RepoActivity
import dev.vikey.devlog.domain.repository.GitRepository

class FakeGitRepository : GitRepository {
    private var activities: List<RepoActivity> = emptyList()
    var callCount: Int = 0
        private set

    fun setActivities(list: List<RepoActivity>) {
        activities = list
    }

    override suspend fun getActivity(
        workspaces: List<String>,
        author: String?,
        since: String,
        until: String,
    ): List<RepoActivity> {
        callCount++
        return activities
    }
}
