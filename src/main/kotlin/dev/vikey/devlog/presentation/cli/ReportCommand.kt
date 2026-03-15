package dev.vikey.devlog.presentation.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import dev.vikey.devlog.data.ConfigLoader
import dev.vikey.devlog.domain.model.ReportType
import dev.vikey.devlog.domain.usecase.GenerateReportUseCase
import dev.vikey.devlog.presentation.renderer.RendererFactory
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class ReportCommand : CliktCommand(name = "report"), KoinComponent {
    override fun help(context: Context): String = "Generate report for a custom date range."

    private val from by option("--from", help = "Start date (YYYY-MM-DD)").required()
    private val to by option("--to", help = "End date (YYYY-MM-DD)").required()
    private val raw by option("--raw", help = "Show raw stats only, no LLM").flag()
    private val workspace by option("--workspace", "-w", help = "Filter by workspace path")
    private val format by option("--format", help = "Output format")
        .choice("terminal", "markdown", "json", "slack")
        .default("terminal")
    private val copy by option("--copy", help = "Copy to clipboard").flag()
    private val output by option("--output", "-o", help = "Save to file")
    private val noCache by option("--no-cache", help = "Skip cache").flag()

    override fun run() = runBlocking {
        validateDate(from, "--from")
        validateDate(to, "--to")
        val config = get<ConfigLoader>().load()
        val effectiveConfig = applyWorkspaceOverride(config, workspace)
        val reportType = ReportType.Range(from = from, to = to)
        val useCase: GenerateReportUseCase = get { parametersOf(effectiveConfig) }
        val report = if (raw) {
            useCase(effectiveConfig, reportType, rawOnly = true, noCache = noCache)
        } else {
            withSpinner("Generating report...") {
                useCase(effectiveConfig, reportType, noCache = noCache)
            }
        }
        val renderer = get<RendererFactory>().create(format)
        renderer.render(report)
        handlePostActions(report.narrative, copy, output)
    }

    private fun validateDate(value: String, flag: String) {
        runCatching { LocalDate.parse(value) }.onFailure {
            throw CliktError(
                "Error: invalid value for '$flag': '$value' is not a valid date. Use YYYY-MM-DD format."
            )
        }
    }
}
