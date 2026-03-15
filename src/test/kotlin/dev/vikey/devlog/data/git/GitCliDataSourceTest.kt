package dev.vikey.devlog.data.git

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class GitCliDataSourceTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var dataSource: GitCliDataSource
    private lateinit var repoDir: File

    @BeforeEach
    fun setup() {
        dataSource = GitCliDataSource()
        repoDir = tempDir.resolve("test-repo").toFile()
        repoDir.mkdirs()
        initGitRepo(repoDir)
    }

    @Test
    fun repoWithCommitsReturnActivity() = runTest {
        createCommit(repoDir, "feat: add login screen", "Login.kt", "class Login {}")
        createCommit(repoDir, "fix: crash on API 26", "Fix.kt", "class Fix {}")

        val activities = dataSource.getReposActivity(
            workspaces = listOf(tempDir.toString()),
            author = null,
            since = "1.year.ago",
            until = "now",
        )

        activities shouldHaveSize 1
        val activity = activities.first()
        activity.name shouldBe "test-repo"
        activity.commits shouldHaveSize 2
        activity.branch.shouldNotBeEmpty()
        activity.totalFilesChanged shouldBeGreaterThan 0
        activity.totalInsertions shouldBeGreaterThan 0
    }

    @Test
    fun emptyRepoReturnsNoActivity() = runTest {
        val activities = dataSource.getReposActivity(
            workspaces = listOf(tempDir.toString()),
            author = null,
            since = "1.year.ago",
            until = "now",
        )

        activities shouldHaveSize 0
    }

    @Test
    fun repoWithNoCommitsInRangeReturnsNoActivity() = runTest {
        createCommit(repoDir, "old commit", "Old.kt", "class Old {}")

        val activities = dataSource.getReposActivity(
            workspaces = listOf(tempDir.toString()),
            author = null,
            since = "2099-01-01",
            until = "2099-12-31",
        )

        activities shouldHaveSize 0
    }

    @Test
    fun workspaceItselfIsGitRepo() = runTest {
        // When workspace itself is a git repo (not a parent of repos)
        val standaloneRepo = tempDir.resolve("standalone").toFile()
        standaloneRepo.mkdirs()
        initGitRepo(standaloneRepo)
        createCommit(standaloneRepo, "feat: standalone", "Main.kt", "fun main() {}")

        val activities = dataSource.getReposActivity(
            workspaces = listOf(standaloneRepo.absolutePath),
            author = null,
            since = "1.year.ago",
            until = "now",
        )

        activities shouldHaveSize 1
        activities.first().name shouldBe "standalone"
    }

    @Test
    fun nonExistentWorkspaceReturnsEmpty() = runTest {
        val activities = dataSource.getReposActivity(
            workspaces = listOf("/nonexistent/path"),
            author = null,
            since = "1.year.ago",
            until = "now",
        )

        activities shouldHaveSize 0
    }

    @Test
    fun multipleWorkspacesReturnActivitiesFromAll(@TempDir secondWorkspace: Path) = runTest {
        createCommit(repoDir, "feat: feature in first", "First.kt", "class First {}")

        val repo2 = secondWorkspace.resolve("second-repo").toFile()
        repo2.mkdirs()
        initGitRepo(repo2)
        createCommit(repo2, "feat: feature in second", "Second.kt", "class Second {}")

        val activities = dataSource.getReposActivity(
            workspaces = listOf(tempDir.toString(), secondWorkspace.toString()),
            author = null,
            since = "1.year.ago",
            until = "now",
        )

        activities shouldHaveSize 2
        activities.map { it.name }.toSet() shouldBe setOf("test-repo", "second-repo")
    }

    @Test
    fun authorFilterReturnsOnlyMatchingCommits() = runTest {
        createCommit(repoDir, "feat: by test user", "Test.kt", "class Test {}")

        val repo2 = tempDir.resolve("other-author-repo").toFile()
        repo2.mkdirs()
        initGitRepo(repo2, email = "other@test.com", name = "Other User")
        createCommit(repo2, "feat: by other", "Other.kt", "class Other {}")

        val activities = dataSource.getReposActivity(
            workspaces = listOf(tempDir.toString()),
            author = "Test User",
            since = "1.year.ago",
            until = "now",
        )

        activities shouldHaveSize 1
        activities.first().name shouldBe "test-repo"
    }

    @Test
    fun parseGitLogExtractsCommits() {
        val output = """
            abc1234567890abcdef1234567890abcdef12345678
            feat: add login
            2026-03-05T10:00:00+01:00

            5${'\t'}2${'\t'}Login.kt
            3${'\t'}0${'\t'}LoginTest.kt

            def5678901234567890abcdef1234567890abcdef12
            fix: crash
            2026-03-05T11:00:00+01:00

            1${'\t'}1${'\t'}Fix.kt
        """.trimIndent()

        val commits = GitCliDataSource.parseGitLog(output)

        commits shouldHaveSize 2
        commits[0].hash shouldBe "abc1234"
        commits[0].message shouldBe "feat: add login"
        commits[0].insertions shouldBe 8
        commits[0].deletions shouldBe 2
        commits[0].filesChanged shouldBe 2
        commits[1].hash shouldBe "def5678"
        commits[1].message shouldBe "fix: crash"
        commits[1].insertions shouldBe 1
        commits[1].deletions shouldBe 1
        commits[1].filesChanged shouldBe 1
    }

    private fun initGitRepo(dir: File, email: String = "test@test.com", name: String = "Test User") {
        runGit(dir, "git", "init")
        runGit(dir, "git", "config", "user.email", email)
        runGit(dir, "git", "config", "user.name", name)
    }

    private fun createCommit(dir: File, message: String, fileName: String, content: String) {
        File(dir, fileName).writeText(content)
        runGit(dir, "git", "add", fileName)
        runGit(dir, "git", "commit", "-m", message)
    }

    private fun runGit(dir: File, vararg command: String) {
        val process = ProcessBuilder(*command)
            .directory(dir)
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        if (exitCode != 0) error("Command failed: ${command.joinToString(" ")}")
    }
}
