package dev.vikey.devlog.presentation.renderer

class RendererFactory {

    fun create(format: String): Renderer = when (format) {
        "terminal" -> TerminalRenderer()
        "markdown" -> MarkdownRenderer()
        "json" -> JsonRenderer()
        "slack" -> SlackRenderer()
        else -> error("Unsupported format: '$format'. Supported: terminal, markdown, json, slack")
    }
}
