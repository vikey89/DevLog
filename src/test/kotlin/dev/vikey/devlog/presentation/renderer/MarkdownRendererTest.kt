package dev.vikey.devlog.presentation.renderer

import dev.vikey.devlog.domain.model.DevlogReport
import dev.vikey.devlog.makeActivity
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.coroutines.test.runTest
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test

class MarkdownRendererTest {

    private val renderer = MarkdownRenderer()

    private fun captureOutput(block: suspend () -> Unit): String {
        val original = System.out
        val buffer = ByteArrayOutputStream()
        System.setOut(PrintStream(buffer))
        try {
            kotlinx.coroutines.runBlocking { block() }
        } finally {
            System.setOut(original)
        }
        return buffer.toString()
    }

    private fun makeReport(
        narrative: String? = null,
        activitiesCount: Int = 1,
    ) = DevlogReport(
        activities = if (activitiesCount > 0) listOf(makeActivity(name = "my-api", commitsCount = 2)) else emptyList(),
        generatedAt = "2026-03-15T10:00:00Z",
        formatType = "today",
        narrative = narrative,
    )

    @Test
    fun `renders markdown header`() = runTest {
        val output = captureOutput { renderer.render(makeReport()) }
        output shouldContain "# DevLog Report"
    }

    @Test
    fun `renders repo name and branch`() = runTest {
        val output = captureOutput { renderer.render(makeReport()) }
        output shouldContain "## my-api (main)"
    }

    @Test
    fun `renders commits as list items`() = runTest {
        val output = captureOutput { renderer.render(makeReport()) }
        output shouldContain "- `hash1` commit message 1"
        output shouldContain "- `hash2` commit message 2"
    }

    @Test
    fun `renders stats`() = runTest {
        val output = captureOutput { renderer.render(makeReport()) }
        output shouldContain "**+30 -6**"
        output shouldContain "3 files"
    }

    @Test
    fun `renders narrative when present`() = runTest {
        val output = captureOutput { renderer.render(makeReport(narrative = "Great work today.")) }
        output shouldContain "---"
        output shouldContain "Great work today."
    }

    @Test
    fun `omits narrative section when null`() = runTest {
        val output = captureOutput { renderer.render(makeReport(narrative = null)) }
        output shouldNotContain "---"
    }
}
