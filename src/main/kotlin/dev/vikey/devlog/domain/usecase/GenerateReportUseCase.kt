package dev.vikey.devlog.domain.usecase

import dev.vikey.devlog.domain.model.AppConfig
import dev.vikey.devlog.domain.model.DevlogReport
import dev.vikey.devlog.domain.model.ReportType
import dev.vikey.devlog.domain.prompt.PromptBuilder
import dev.vikey.devlog.domain.repository.CacheRepository
import dev.vikey.devlog.domain.repository.GitRepository
import dev.vikey.devlog.domain.repository.LlmRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

private const val DAYS_IN_WEEK = 7

/**
 * Orchestrates report generation: fetches git activity, calls the LLM for a narrative,
 * and caches the result. Supports raw-only mode (no LLM) and cache bypass.
 */
class GenerateReportUseCase(
    private val gitRepo: GitRepository,
    private val llmRepo: LlmRepository,
    private val cacheRepo: CacheRepository,
    private val promptBuilder: PromptBuilder,
) {
    suspend operator fun invoke(
        config: AppConfig,
        reportType: ReportType,
        rawOnly: Boolean = false,
        noCache: Boolean = false,
    ): DevlogReport {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        val params = resolveParams(reportType, today)
        val cacheKey = "${params.cachePrefix}_${params.cacheSuffix}_${config.workspaceHash}"
        val cached = if (noCache) null else cacheRepo.get(cacheKey)

        return cached ?: generate(config, reportType, params, cacheKey, rawOnly)
    }

    private suspend fun generate(
        config: AppConfig,
        reportType: ReportType,
        params: ResolvedParams,
        cacheKey: String,
        rawOnly: Boolean,
    ): DevlogReport {
        val activities = gitRepo.getActivity(
            workspaces = config.workspaces,
            author = config.author,
            since = params.since,
            until = params.until,
        )

        val report = DevlogReport(
            activities = activities,
            generatedAt = Clock.System.now().toString(),
            formatType = params.formatType,
            dateRange = params.dateRange,
        )
        if (rawOnly) return report

        val prompt = promptBuilder.build(reportType, activities, config.language)
        val result = llmRepo.generate(prompt)
        val narrative = result.getOrNull()
        return if (narrative != null) {
            val finalReport = report.withNarrative(narrative)
            cacheRepo.save(cacheKey, finalReport)
            finalReport
        } else {
            report.withError(result.exceptionOrNull()?.message ?: "Unknown LLM error")
        }
    }
}

private data class ResolvedParams(
    val since: String,
    val until: String,
    val formatType: String,
    val dateRange: String,
    val cachePrefix: String,
    val cacheSuffix: String,
)

private fun resolveParams(type: ReportType, today: LocalDate): ResolvedParams =
    when (type) {
        is ReportType.Daily -> dailyParams(today)
        is ReportType.Yesterday -> yesterdayParams(today)
        is ReportType.Weekly -> weeklyParams(today)
        is ReportType.Standup -> standupParams(today)
        is ReportType.Range -> rangeParams(type)
    }

private fun dailyParams(today: LocalDate) = ResolvedParams(
    since = today.toString(), until = "now", formatType = "today",
    dateRange = today.toString(), cachePrefix = "daily", cacheSuffix = "today_$today",
)

private fun yesterdayParams(today: LocalDate): ResolvedParams {
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    return ResolvedParams(
        since = yesterday.toString(), until = today.toString(), formatType = "yesterday",
        dateRange = yesterday.toString(),
        cachePrefix = "daily", cacheSuffix = "yesterday_$today",
    )
}

private fun weeklyParams(today: LocalDate): ResolvedParams {
    val weekAgo = today.minus(DAYS_IN_WEEK, DateTimeUnit.DAY)
    return ResolvedParams(
        since = weekAgo.toString(), until = "now", formatType = "weekly",
        dateRange = "$weekAgo → $today", cachePrefix = "weekly", cacheSuffix = today.toString(),
    )
}

private fun standupParams(today: LocalDate): ResolvedParams {
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    return ResolvedParams(
        since = yesterday.toString(), until = "now", formatType = "standup",
        dateRange = "$yesterday → $today", cachePrefix = "standup", cacheSuffix = today.toString(),
    )
}

private fun rangeParams(type: ReportType.Range) = ResolvedParams(
    since = type.from, until = type.to, formatType = "range",
    dateRange = "${type.from} → ${type.to}",
    cachePrefix = "range", cacheSuffix = "${type.from}_${type.to}",
)
