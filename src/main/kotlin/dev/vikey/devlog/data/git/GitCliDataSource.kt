package dev.vikey.devlog.data.git

import dev.vikey.devlog.domain.model.CommitInfo
import dev.vikey.devlog.domain.model.RepoActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

class GitCliDataSource : GitDataSource {

    override suspend fun getReposActivity(
        workspaces: List<String>,
        author: String?,
        since: String,
        until: String,
    ): List<RepoActivity> = withContext(Dispatchers.IO) {
        workspaces.flatMap { workspace ->
            findGitRepos(Path.of(workspace)).mapNotNull { repoDir ->
                runCatching { getRepoActivity(repoDir, author, since, until) }
                    .getOrNull()
                    ?.takeIf { !it.isEmpty }
            }
        }
    }

    private fun findGitRepos(workspace: Path): List<File> {
        if (!Files.exists(workspace)) return emptyList()
        val workspaceFile = workspace.toFile()
        // If workspace itself is a git repo, return only it; otherwise scan recursively
        return if (File(workspaceFile, ".git").exists()) {
            listOf(workspaceFile)
        } else {
            Files.walk(workspace, MAX_SCAN_DEPTH).use { stream ->
                stream.filter { it.isDirectory() }
                    .filter { it.fileName.toString() != ".git" }
                    .filter { Files.exists(it.resolve(".git")) }
                    .map { it.toFile() }
                    .toList()
            }
        }
    }

    private fun getRepoActivity(
        repoDir: File,
        author: String?,
        since: String,
        until: String,
    ): RepoActivity {
        val branch = getCurrentBranch(repoDir)
        val commits = getCommits(repoDir, author, since, until)
        return RepoActivity(
            name = repoDir.name,
            path = repoDir.absolutePath,
            branch = branch,
            commits = commits,
            totalInsertions = commits.sumOf { it.insertions },
            totalDeletions = commits.sumOf { it.deletions },
            totalFilesChanged = commits.sumOf { it.filesChanged },
        )
    }

    private fun getCurrentBranch(repoDir: File): String {
        val result = runGit(repoDir, "git", "rev-parse", "--abbrev-ref", "HEAD")
        return result.trim().ifEmpty { "unknown" }
    }

    private fun getCommits(
        repoDir: File,
        author: String?,
        since: String,
        until: String,
    ): List<CommitInfo> {
        val args = mutableListOf(
            "git", "log",
            "--format=%H%n%s%n%aI",
            "--numstat",
            "--since=$since",
            "--until=$until",
        )
        if (author != null) {
            args.add("--author=$author")
        }

        val output = runGitArgs(repoDir, args)
        if (output.isBlank()) return emptyList()

        return parseGitLog(output)
    }

    private fun runGit(repoDir: File, vararg command: String): String =
        runGitArgs(repoDir, command.toList())

    private fun runGitArgs(repoDir: File, command: List<String>): String {
        val process = ProcessBuilder(command)
            .directory(repoDir)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        if (exitCode != 0) error("git command failed (exit $exitCode): ${command.joinToString(" ")}")
        return output
    }

    companion object {
        private const val MAX_SCAN_DEPTH = 4
        private const val MIN_HASH_LENGTH = 7
        private const val NUMSTAT_COLUMNS = 3
        private const val FORMAT_LINES = 3

        internal fun parseGitLog(output: String): List<CommitInfo> {
            val lines = output.lines()
            val commits = mutableListOf<CommitInfo>()
            var i = 0
            while (i < lines.size) {
                i = skipToNextCommitHash(lines, i)
                if (i + FORMAT_LINES > lines.size) break
                val parsed = parseOneCommit(lines, i, lines[i].trim())
                i = parsed.first
                commits.add(parsed.second)
            }
            return commits
        }

        private fun skipToNextCommitHash(lines: List<String>, start: Int): Int {
            var i = start
            while (i < lines.size && (lines[i].isBlank() || lines[i].trim().length < MIN_HASH_LENGTH)) {
                i++
            }
            return i
        }

        private fun parseOneCommit(lines: List<String>, start: Int, hash: String): Pair<Int, CommitInfo> {
            val message = lines[start + 1].trim()
            val timestamp = lines[start + 2].trim()
            var i = start + FORMAT_LINES
            if (i < lines.size && lines[i].isBlank()) i++
            val stats = parseNumstat(lines, i)
            return stats.nextIndex to CommitInfo(
                hash = hash.take(MIN_HASH_LENGTH),
                message = message,
                timestamp = timestamp,
                filesChanged = stats.filesChanged,
                insertions = stats.insertions,
                deletions = stats.deletions,
            )
        }

        private fun parseNumstat(lines: List<String>, start: Int): NumstatResult {
            var insertions = 0
            var deletions = 0
            var filesChanged = 0
            var i = start
            while (i < lines.size && lines[i].contains('\t')) {
                val parts = lines[i].split("\t")
                if (parts.size >= NUMSTAT_COLUMNS) {
                    insertions += parts[0].toIntOrNull() ?: 0
                    deletions += parts[1].toIntOrNull() ?: 0
                    filesChanged++
                }
                i++
            }
            return NumstatResult(insertions, deletions, filesChanged, i)
        }

        private data class NumstatResult(
            val insertions: Int,
            val deletions: Int,
            val filesChanged: Int,
            val nextIndex: Int,
        )
    }
}
