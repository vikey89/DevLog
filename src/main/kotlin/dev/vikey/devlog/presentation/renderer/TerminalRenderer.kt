package dev.vikey.devlog.presentation.renderer

import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.rendering.TextColors.brightGreen
import com.github.ajalt.mordant.rendering.TextColors.brightRed
import com.github.ajalt.mordant.rendering.TextColors.brightWhite
import com.github.ajalt.mordant.rendering.TextColors.brightYellow
import com.github.ajalt.mordant.rendering.TextColors.cyan
import com.github.ajalt.mordant.rendering.TextColors.green
import com.github.ajalt.mordant.rendering.TextColors.red
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.rendering.TextStyles.dim
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import dev.vikey.devlog.APP_VERSION
import dev.vikey.devlog.domain.model.DevlogReport
import dev.vikey.devlog.domain.model.RepoActivity

class TerminalRenderer(
    private val terminal: Terminal = Terminal(),
) : Renderer {

    override suspend fun render(report: DevlogReport) {
        renderBanner(report)
        if (report.isEmpty) {
            terminal.println(dim("  ⌀ No git activity found."))
            terminal.println()
            return
        }
        renderTable(report.activities)
        renderSummaryBars(report.activities)
        report.error?.let { renderError(it) }
        report.narrative?.let { renderNarrative(it) }
    }

private fun renderBanner(report: DevlogReport) {
        val dateInfo = report.dateRange.ifEmpty {
            report.generatedAt.take(DATE_PREFIX_LEN)
        }
        val titleLine = "  " +
            (bold + brightWhite)("DEVLOG") +
            dim(" v$APP_VERSION")
        val dateLine = "  " +
            cyan(report.formatType) +
            dim(" · ") +
            brightWhite(dateInfo)
        terminal.println()
        renderBookArt(titleLine, dateLine)
        terminal.println()
    }

    private fun renderBookArt(titleLine: String, dateLine: String) {
        val cv = COVER
        val sp = SPINE
        val bp = BLANK_PAGE
        val tx = TEXT_LINE
        terminal.println("  " + cv("▄▄▄▄▄▄▄▄▄▄") + sp("▄▄") + cv("▄▄▄▄▄▄▄▄▄▄"))
        terminal.println("  " + cv("█") + bp("         ") + sp("██") + bp("         ") + cv("█"))
        terminal.println("  " + cv("█") + tx(" ▀▀ ▀▀▀▀ ") + sp("██") + tx(" ▀▀▀ ▀▀  ") + cv("█") + titleLine)
        terminal.println("  " + cv("█") + tx(" ▀▀▀ ▀▀▀ ") + sp("██") + tx(" ▀▀ ▀▀▀  ") + cv("█") + dateLine)
        terminal.println("  " + cv("█") + tx(" ▀▀ ▀▀ ▀ ") + sp("██") + tx(" ▀▀▀ ▀▀▀ ") + cv("█"))
        terminal.println("  " + cv("█") + bp("         ") + sp("██") + bp("         ") + cv("█"))
        terminal.println("  " + cv("▀▀▀▀▀▀▀▀▀▀") + sp("▀▀") + cv("▀▀▀▀▀▀▀▀▀▀"))
    }

    private fun renderTable(activities: List<RepoActivity>) {
        val t = table {
            header {
                row(
                    dim("REPO"),
                    dim("BRANCH"),
                    dim("COMMITS"),
                    dim("+/-"),
                    dim("FILES"),
                )
            }
            body {
                activities.forEach { a ->
                    row(
                        cyan(a.name),
                        dim(a.branch),
                        buildCommitBar(a.commits.size) +
                            " " + brightWhite(a.commits.size.toString()),
                        brightGreen("+${a.totalInsertions}") +
                            " " + brightRed("-${a.totalDeletions}"),
                        brightYellow(a.totalFilesChanged.toString()),
                    )
                }
            }
        }
        terminal.println(t)
    }

    private fun buildCommitBar(count: Int): String {
        val filled = count.coerceAtMost(COMMIT_BAR_MAX)
        val empty = COMMIT_BAR_MAX - filled
        return cyan("▮".repeat(filled)) + dim("·".repeat(empty))
    }

    private fun renderSummaryBars(activities: List<RepoActivity>) {
        val totalIns = activities.sumOf { it.totalInsertions }
        val totalDel = activities.sumOf { it.totalDeletions }
        val max = maxOf(totalIns, totalDel, 1)
        val insFilled = scale(totalIns, max)
        val delFilled = scale(totalDel, max)
        terminal.println()
        terminal.println(
            green("  ++ ") +
                brightGreen("▮".repeat(insFilled)) +
                dim("·".repeat(SUMMARY_BAR_WIDTH - insFilled)) +
                green(" $totalIns ins")
        )
        terminal.println(
            red("  -- ") +
                brightRed("▮".repeat(delFilled)) +
                dim("·".repeat(SUMMARY_BAR_WIDTH - delFilled)) +
                red(" $totalDel del")
        )
    }

    private fun scale(value: Int, max: Int): Int {
        if (value == 0) return 0
        return (value * SUMMARY_BAR_WIDTH / max).coerceAtLeast(1)
    }

    private fun renderError(message: String) {
        terminal.println()
        terminal.println(
            "  " + red("✗") + (bold + brightRed)(" LLM Error: ") + red(message),
        )
        terminal.println()
    }

    private fun renderNarrative(text: String) {
        terminal.println()
        terminal.println(
            "  " + cyan("◆") +
                (bold + brightWhite)(" NARRATIVE ") +
                dim(SEPARATOR)
        )
        terminal.println()
        text.lines().forEach { line ->
            terminal.println(styleLine(line))
        }
        terminal.println()
        terminal.println(dim("  $SEPARATOR$SEPARATOR"))
        terminal.println()
    }

    private fun styleLine(line: String): String {
        val trimmed = line.trimStart()
        val inline = { t: String ->
            BOLD_REGEX.replace(t) { (bold + brightYellow)(it.groupValues[1]) }
        }
        return when {
            trimmed.startsWith("## ") ->
                "  " + (bold + brightWhite)(trimmed.removePrefix("## "))
            trimmed.startsWith("# ") ->
                "  " + (bold + cyan)(trimmed.removePrefix("# "))
            trimmed.startsWith("- ") ->
                "  " + cyan("▸ ") + inline(trimmed.removePrefix("- "))
            trimmed == "---" ->
                dim("  $SEPARATOR")
            trimmed.isEmpty() -> ""
            else -> "  " + inline(trimmed)
        }
    }

    companion object {
        private const val COMMIT_BAR_MAX = 10
        private const val SUMMARY_BAR_WIDTH = 25
        private const val DATE_PREFIX_LEN = 10
        private const val SEPARATOR = "─────────────────────────"
        private val BOLD_REGEX = Regex("\\*\\*(.+?)\\*\\*")
        private val PAGE = TextColors.rgb("#dce8ec")
        private val TEXT_LINE = TextColors.rgb("#8fa8ad") on PAGE
        private val BLANK_PAGE = PAGE on PAGE
        private val COVER = TextColors.rgb("#8b6b28")
        private val SPINE = TextColors.rgb("#4a5858")
    }
}
