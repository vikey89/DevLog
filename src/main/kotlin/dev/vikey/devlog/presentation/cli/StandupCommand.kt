package dev.vikey.devlog.presentation.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.choice
import dev.vikey.devlog.data.ConfigLoader
import dev.vikey.devlog.domain.model.ReportType
import dev.vikey.devlog.domain.usecase.GenerateReportUseCase
import dev.vikey.devlog.presentation.renderer.RendererFactory
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

class StandupCommand : CliktCommand(name = "standup"), KoinComponent {
    override fun help(context: Context): String = "Quick standup update from today's activity."

    private val raw by option("--raw", help = "Show raw stats only, no LLM").flag()
    private val workspace by option("--workspace", "-w", help = "Filter by workspace path")
    private val format by option("--format", help = "Output format")
        .choice("terminal", "markdown", "json", "slack")
        .default("terminal")
    private val copy by option("--copy", help = "Copy to clipboard").flag()
    private val output by option("--output", "-o", help = "Save to file")
    private val noCache by option("--no-cache", help = "Skip cache").flag()

    override fun run() = runBlocking {
        val config = get<ConfigLoader>().load()
        val effectiveConfig = applyWorkspaceOverride(config, workspace)
        val useCase: GenerateReportUseCase = get { parametersOf(effectiveConfig) }
        val report = if (raw) {
            useCase(effectiveConfig, ReportType.Standup, rawOnly = true, noCache = noCache)
        } else {
            withSpinner("Generating report...") {
                useCase(effectiveConfig, ReportType.Standup, noCache = noCache)
            }
        }
        val renderer = get<RendererFactory>().create(format)
        renderer.render(report)
        handlePostActions(report.narrative, copy, output)
    }
}
