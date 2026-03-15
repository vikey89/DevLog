package dev.vikey.devlog.presentation.renderer

import dev.vikey.devlog.domain.model.DevlogReport

class MarkdownRenderer : Renderer {

    override suspend fun render(report: DevlogReport) {
        println(toMarkdown(report))
    }

    private fun toMarkdown(report: DevlogReport): String = buildString {
        appendLine("# DevLog Report")
        appendLine()
        report.activities.forEach { a ->
            appendLine("## ${a.name} (${a.branch})")
            appendLine()
            a.commits.forEach { c ->
                appendLine("- `${c.hash}` ${c.message}")
            }
            appendLine()
            appendLine("**+${a.totalInsertions} -${a.totalDeletions}** | ${a.totalFilesChanged} files")
            appendLine()
        }
        report.narrative?.let {
            appendLine("---")
            appendLine()
            appendLine(it)
        }
    }
}
