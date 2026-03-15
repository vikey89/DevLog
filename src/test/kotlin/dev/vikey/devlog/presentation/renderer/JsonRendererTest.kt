package dev.vikey.devlog.presentation.renderer

import dev.vikey.devlog.domain.model.DevlogReport
import dev.vikey.devlog.makeActivity
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test

class JsonRendererTest {

    private val renderer = JsonRenderer()

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
        activities = listOf(makeActivity(name = "my-api", commitsCount = 1)),
        generatedAt = "2026-03-15T10:00:00Z",
        formatType = "today",
        narrative = narrative,
    )

    @Test
    fun `renders valid JSON`() = runTest {
        val output = captureOutput { renderer.render(makeReport()) }
        val json = Json.parseToJsonElement(output) as JsonObject
        json["formatType"]!!.jsonPrimitive.content shouldContain "today"
    }

    @Test
    fun `JSON contains activities`() = runTest {
        val output = captureOutput { renderer.render(makeReport()) }
        val json = Json.parseToJsonElement(output) as JsonObject
        val activities = json["activities"]!!.jsonArray
        activities.size shouldBe 1
    }

    @Test
    fun `JSON includes narrative when present`() = runTest {
        val output = captureOutput { renderer.render(makeReport(narrative = "Summary.")) }
        output shouldContain "Summary."
    }
}
