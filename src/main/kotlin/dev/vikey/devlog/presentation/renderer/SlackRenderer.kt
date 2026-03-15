package dev.vikey.devlog.presentation.renderer

import dev.vikey.devlog.domain.model.DevlogReport

class SlackRenderer : Renderer {

    override suspend fun render(report: DevlogReport) {
        println(toSlack(report))
    }

    private fun toSlack(report: DevlogReport): String = buildString {
        appendLine("*DevLog Report*")
        appendLine()
        report.activities.forEach { a ->
            appendLine("*${a.name}* (`${a.branch}`)")
            a.commits.forEach { c ->
                appendLine("  \u2022 `${c.hash}` ${c.message}")
            }
            appendLine("  _+${a.totalInsertions} -${a.totalDeletions} | ${a.totalFilesChanged} files_")
            appendLine()
        }
        report.narrative?.let {
            appendLine("---")
            appendLine()
            appendLine(it)
        }
    }
}
