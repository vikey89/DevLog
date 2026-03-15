package dev.vikey.devlog.presentation.renderer

import dev.vikey.devlog.domain.model.DevlogReport
import dev.vikey.devlog.makeActivity
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlinx.coroutines.test.runTest
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test

class SlackRendererTest {

    private val renderer = SlackRenderer()

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

    private fun makeReport(narrative: String? = null) = DevlogReport(
        activities = listOf(makeActivity(name = "my-api", commitsCount = 2)),
        generatedAt = "2026-03-15T10:00:00Z",
        formatType = "today",
        narrative = narrative,
    )

    @Test
    fun `renders slack bold header`() = runTest {
        val output = captureOutput { renderer.render(makeReport()) }
        output shouldContain "*DevLog Report*"
    }

    @Test
    fun `renders repo name in slack bold`() = runTest {
        val output = captureOutput { renderer.render(makeReport()) }
        output shouldContain "*my-api*"
        output shouldContain "`main`"
    }

    @Test
    fun `renders commits as bullet points`() = runTest {
        val output = captureOutput { renderer.render(makeReport()) }
        output shouldContain "\u2022 `hash1` commit message 1"
    }

    @Test
    fun `renders stats in italic`() = runTest {
        val output = captureOutput { renderer.render(makeReport()) }
        output shouldContain "_+30 -6 | 3 files_"
    }

    @Test
    fun `renders narrative when present`() = runTest {
        val output = captureOutput { renderer.render(makeReport(narrative = "Summary here.")) }
        output shouldContain "Summary here."
    }

    @Test
    fun `omits narrative section when null`() = runTest {
        val output = captureOutput { renderer.render(makeReport(narrative = null)) }
        output shouldNotContain "---"
    }
}
