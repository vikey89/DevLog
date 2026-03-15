package dev.vikey.devlog.domain.usecase

import dev.vikey.devlog.domain.model.DevlogReport
import dev.vikey.devlog.domain.model.ReportType
import dev.vikey.devlog.domain.prompt.PromptBuilder
import dev.vikey.devlog.fakes.FakeCacheRepository
import dev.vikey.devlog.fakes.FakeGitRepository
import dev.vikey.devlog.fakes.FakeLlmRepository
import dev.vikey.devlog.makeActivity
import dev.vikey.devlog.makeTestConfig
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GenerateReportUseCaseTest {
    private lateinit var gitRepo: FakeGitRepository
    private lateinit var llmRepo: FakeLlmRepository
    private lateinit var cacheRepo: FakeCacheRepository
    private lateinit var useCase: GenerateReportUseCase

    private val config = makeTestConfig()

    @BeforeEach
    fun setup() {
        gitRepo = FakeGitRepository()
        llmRepo = FakeLlmRepository()
        cacheRepo = FakeCacheRepository()
        useCase = GenerateReportUseCase(gitRepo, llmRepo, cacheRepo, PromptBuilder())
    }

    @Test
    fun `daily with no commits returns empty report`() = runTest {
        gitRepo.setActivities(emptyList())

        val report = useCase(config, ReportType.Daily)

        report.isEmpty shouldBe true
        report.narrative shouldBe "Test narrative"
    }

    @Test
    fun `daily calls llm when not raw`() = runTest {
        gitRepo.setActivities(listOf(makeActivity()))

        val report = useCase(config, ReportType.Daily)

        llmRepo.callCount shouldBe 1
        report.narrative.shouldNotBeNull()
        report.narrative shouldBe "Test narrative"
    }

    @Test
    fun `daily skips llm when raw is true`() = runTest {
        gitRepo.setActivities(listOf(makeActivity()))

        val report = useCase(config, ReportType.Daily, rawOnly = true)

        llmRepo.callCount shouldBe 0
        report.narrative.shouldBeNull()
    }

    @Test
    fun `daily uses cache when available`() = runTest {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val cachedReport = DevlogReport(
            activities = listOf(makeActivity(name = "cached-repo")),
            generatedAt = Clock.System.now().toString(),
            formatType = "today",
        ).withNarrative("Cached narrative")

        val cacheKey = "daily_today_${today}_${config.workspaceHash}"
        cacheRepo.setCached(cacheKey, cachedReport)

        val report = useCase(config, ReportType.Daily)

        report.narrative shouldBe "Cached narrative"
        gitRepo.callCount shouldBe 0
        llmRepo.callCount shouldBe 0
    }

    @Test
    fun `daily saves to cache after generation`() = runTest {
        gitRepo.setActivities(listOf(makeActivity()))

        useCase(config, ReportType.Daily)

        cacheRepo.saveCount shouldBe 1
        cacheRepo.lastSavedKey.shouldNotBeNull()
        cacheRepo.lastSavedKey!! shouldContain "daily_"
    }

    @Test
    fun `weekly with multiple repos generates narrative`() = runTest {
        gitRepo.setActivities(
            listOf(
                makeActivity(name = "repo-a"),
                makeActivity(name = "repo-b"),
            ),
        )

        val report = useCase(config, ReportType.Weekly)

        report.formatType shouldBe "weekly"
        report.narrative.shouldNotBeNull()
        report.activities.size shouldBe 2
        llmRepo.callCount shouldBe 1
        cacheRepo.saveCount shouldBe 1
    }

    @Test
    fun `weekly with no commits returns empty report`() = runTest {
        gitRepo.setActivities(emptyList())

        val report = useCase(config, ReportType.Weekly)

        report.isEmpty shouldBe true
        report.formatType shouldBe "weekly"
    }

    @Test
    fun `weekly raw mode skips llm`() = runTest {
        gitRepo.setActivities(listOf(makeActivity()))

        val report = useCase(config, ReportType.Weekly, rawOnly = true)

        report.formatType shouldBe "weekly"
        report.narrative.shouldBeNull()
        llmRepo.callCount shouldBe 0
    }

    @Test
    fun `weekly uses cache when available`() = runTest {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val cachedReport = DevlogReport(
            activities = listOf(makeActivity(name = "cached-repo")),
            generatedAt = Clock.System.now().toString(),
            formatType = "weekly",
        ).withNarrative("Cached weekly")

        val cacheKey = "weekly_${today}_${config.workspaceHash}"
        cacheRepo.setCached(cacheKey, cachedReport)

        val report = useCase(config, ReportType.Weekly)

        report.narrative shouldBe "Cached weekly"
        gitRepo.callCount shouldBe 0
        llmRepo.callCount shouldBe 0
    }

    @Test
    fun `weekly noCache bypasses cache`() = runTest {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val cachedReport = DevlogReport(
            activities = listOf(makeActivity(name = "cached-repo")),
            generatedAt = Clock.System.now().toString(),
            formatType = "weekly",
        ).withNarrative("Cached weekly")

        val cacheKey = "weekly_${today}_${config.workspaceHash}"
        cacheRepo.setCached(cacheKey, cachedReport)
        gitRepo.setActivities(listOf(makeActivity()))

        val report = useCase(config, ReportType.Weekly, noCache = true)

        gitRepo.callCount shouldBe 1
        llmRepo.callCount shouldBe 1
    }

    @Test
    fun `standup with activity generates narrative`() = runTest {
        gitRepo.setActivities(listOf(makeActivity()))

        val report = useCase(config, ReportType.Standup)

        report.formatType shouldBe "standup"
        report.narrative.shouldNotBeNull()
        llmRepo.callCount shouldBe 1
        cacheRepo.saveCount shouldBe 1
    }

    @Test
    fun `standup raw mode skips llm`() = runTest {
        gitRepo.setActivities(listOf(makeActivity()))

        val report = useCase(config, ReportType.Standup, rawOnly = true)

        report.formatType shouldBe "standup"
        report.narrative.shouldBeNull()
        llmRepo.callCount shouldBe 0
    }

    @Test
    fun `standup with no commits returns empty report`() = runTest {
        gitRepo.setActivities(emptyList())

        val report = useCase(config, ReportType.Standup)

        report.isEmpty shouldBe true
        report.formatType shouldBe "standup"
    }

    @Test
    fun `range report generates with correct date range`() = runTest {
        gitRepo.setActivities(listOf(makeActivity()))

        val report = useCase(config, ReportType.Range("2026-03-01", "2026-03-05"))

        report.formatType shouldBe "range"
        report.dateRange shouldContain "2026-03-01"
        report.dateRange shouldContain "2026-03-05"
        report.narrative.shouldNotBeNull()
        llmRepo.callCount shouldBe 1
    }

    @Test
    fun `yesterday report has correct format type`() = runTest {
        gitRepo.setActivities(listOf(makeActivity()))

        val report = useCase(config, ReportType.Yesterday)

        report.formatType shouldBe "yesterday"
        report.narrative.shouldNotBeNull()
    }
}
