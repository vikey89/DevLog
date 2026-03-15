package dev.vikey.devlog.presentation.renderer

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.test.Test

class RendererFactoryTest {

    private val factory = RendererFactory()

    @Test
    fun `creates terminal renderer`() {
        factory.create("terminal").shouldBeInstanceOf<TerminalRenderer>()
    }

    @Test
    fun `creates markdown renderer`() {
        factory.create("markdown").shouldBeInstanceOf<MarkdownRenderer>()
    }

    @Test
    fun `creates json renderer`() {
        factory.create("json").shouldBeInstanceOf<JsonRenderer>()
    }

    @Test
    fun `creates slack renderer`() {
        factory.create("slack").shouldBeInstanceOf<SlackRenderer>()
    }

    @Test
    fun `throws for unsupported format`() {
        shouldThrow<IllegalStateException> {
            factory.create("xml")
        }
    }
}
