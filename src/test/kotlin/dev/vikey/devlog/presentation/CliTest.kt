package dev.vikey.devlog.presentation

import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.testing.test
import dev.vikey.devlog.presentation.cli.DevlogCommand
import dev.vikey.devlog.presentation.cli.InitCommand
import dev.vikey.devlog.presentation.cli.ReportCommand
import dev.vikey.devlog.presentation.cli.StandupCommand
import dev.vikey.devlog.presentation.cli.TodayCommand
import dev.vikey.devlog.presentation.cli.WeekCommand
import dev.vikey.devlog.presentation.cli.YesterdayCommand
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class CliTest {

    @Test
    fun `devlog help does not throw`() {
        val result = DevlogCommand().test("--help")
        result.statusCode shouldBe 0
    }

    @Test
    fun `devlog help contains command description`() {
        val result = DevlogCommand().test("--help")
        result.output shouldContain "devlog"
    }

    @Test
    fun `devlog with subcommands lists them in help`() {
        val command = DevlogCommand().subcommands(
            TodayCommand(),
            WeekCommand(),
            StandupCommand(),
            YesterdayCommand(),
            ReportCommand(),
            InitCommand(),
        )
        val result = command.test("--help")

        result.statusCode shouldBe 0
        result.output shouldContain "today"
        result.output shouldContain "week"
        result.output shouldContain "standup"
        result.output shouldContain "yesterday"
        result.output shouldContain "report"
        result.output shouldContain "init"
    }
}
