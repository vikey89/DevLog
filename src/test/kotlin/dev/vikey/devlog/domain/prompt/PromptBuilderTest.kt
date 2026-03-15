package dev.vikey.devlog.domain.prompt

import dev.vikey.devlog.domain.model.ReportType
import dev.vikey.devlog.makeActivity
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlin.test.Test

class PromptBuilderTest {

    private val builder = PromptBuilder()
    private val activities = listOf(makeActivity(name = "my-api", commitsCount = 2))

    @Test
    fun `daily report includes daily header and language`() {
        val prompt = builder.build(ReportType.Daily, activities, "english")
        prompt shouldContain "daily development log"
        prompt shouldContain "Language: english"
    }

    @Test
    fun `yesterday report uses daily header`() {
        val prompt = builder.build(ReportType.Yesterday, activities, "italian")
        prompt shouldContain "daily development log"
        prompt shouldContain "Language: italian"
    }

    @Test
    fun `weekly report includes weekly header and grouping instruction`() {
        val prompt = builder.build(ReportType.Weekly, activities, "english")
        prompt shouldContain "weekly development summary"
        prompt shouldContain "Group by theme"
    }

    @Test
    fun `standup report includes standup header and short instruction`() {
        val prompt = builder.build(ReportType.Standup, activities, "english")
        prompt shouldContain "standup update"
        prompt shouldContain "Keep it very short"
    }

    @Test
    fun `range report includes date range in header`() {
        val prompt = builder.build(ReportType.Range("2026-03-01", "2026-03-07"), activities, "english")
        prompt shouldContain "2026-03-01"
        prompt shouldContain "2026-03-07"
    }

    @Test
    fun `prompt includes repo name and branch`() {
        val prompt = builder.build(ReportType.Daily, activities, "english")
        prompt shouldContain "my-api"
        prompt shouldContain "branch: main"
    }

    @Test
    fun `prompt includes commit messages`() {
        val prompt = builder.build(ReportType.Daily, activities, "english")
        prompt shouldContain "commit message 1"
        prompt shouldContain "commit message 2"
    }

    @Test
    fun `prompt includes stats`() {
        val prompt = builder.build(ReportType.Daily, activities, "english")
        prompt shouldContain "+30 -6"
        prompt shouldContain "3 files"
    }

    @Test
    fun `empty activities produces prompt with header only`() {
        val prompt = builder.build(ReportType.Daily, emptyList(), "english")
        prompt shouldContain "daily development log"
        prompt shouldNotContain "==="
    }
}
