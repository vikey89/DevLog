package dev.vikey.devlog

import dev.vikey.devlog.data.git.GitCliDataSource
import dev.vikey.devlog.data.repository.GitRepositoryImpl
import dev.vikey.devlog.domain.model.ReportType
import dev.vikey.devlog.domain.prompt.PromptBuilder
import dev.vikey.devlog.domain.usecase.GenerateReportUseCase
import dev.vikey.devlog.fakes.FakeCacheRepository
import dev.vikey.devlog.fakes.FakeLlmRepository
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class IntegrationTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var repoDir: File
    private lateinit var fakeLlm: FakeLlmRepository
    private lateinit var fakeCache: FakeCacheRepository
    private lateinit var generateReport: GenerateReportUseCase

    @BeforeEach
    fun setup() {
        repoDir = tempDir.resolve("test-project").toFile()
        repoDir.mkdirs()
        initGitRepo(repoDir)

        createCommit(repoDir, "feat: add login screen", "Login.kt", "class Login {}")
        createCommit(repoDir, "fix: crash on API 26", "Fix.kt", "class Fix {}")
        createCommit(repoDir, "refactor: extract usecase", "UseCase.kt", "class UseCase {}")

        fakeLlm = FakeLlmRepository(response = Result.success("Today I worked on authentication and stability fixes."))
        fakeCache = FakeCacheRepository()

        val gitDataSource = GitCliDataSource()
        val gitRepo = GitRepositoryImpl(gitDataSource)

        generateReport = GenerateReportUseCase(gitRepo, fakeLlm, fakeCache, PromptBuilder())
    }

    @Test
    fun `end to end daily report with real git and fake llm`() = runTest {
        val config = makeTestConfig(
            workspaces = listOf(tempDir.toString()),
        )

        val report = generateReport(
            config,
            ReportType.Range(from = "1.year.ago", to = "now"),
        )

        report.activities shouldHaveSize 1
        val activity = report.activities.first()
        activity.name shouldBe "test-project"
        activity.commits shouldHaveSize 3
        activity.totalInsertions shouldBeGreaterThan 0
        activity.totalFilesChanged shouldBeGreaterThan 0
        activity.branch.shouldNotBeEmpty()

        report.narrative shouldContain "authentication"
        fakeLlm.callCount shouldBe 1
        fakeCache.saveCount shouldBe 1
    }

    @Test
    fun `end to end raw report skips llm`() = runTest {
        val config = makeTestConfig(
            workspaces = listOf(tempDir.toString()),
        )

        val report = generateReport(
            config,
            ReportType.Range(from = "1.year.ago", to = "now"),
            rawOnly = true,
        )

        report.activities shouldHaveSize 1
        report.activities.first().commits shouldHaveSize 3
        report.narrative shouldBe null
        fakeLlm.callCount shouldBe 0
        fakeCache.saveCount shouldBe 0
    }

    private fun initGitRepo(dir: File) {
        runGit(dir, "git", "init")
        runGit(dir, "git", "config", "user.email", "test@test.com")
        runGit(dir, "git", "config", "user.name", "Test User")
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
