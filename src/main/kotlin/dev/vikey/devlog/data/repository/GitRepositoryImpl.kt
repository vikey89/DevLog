package dev.vikey.devlog.data.repository

import dev.vikey.devlog.data.git.GitDataSource
import dev.vikey.devlog.domain.model.RepoActivity
import dev.vikey.devlog.domain.repository.GitRepository

class GitRepositoryImpl(
    private val dataSource: GitDataSource,
) : GitRepository {

    override suspend fun getActivity(
        workspaces: List<String>,
        author: String?,
        since: String,
        until: String,
    ): List<RepoActivity> = dataSource.getReposActivity(workspaces, author, since, until)
}
