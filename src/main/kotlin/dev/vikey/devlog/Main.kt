package dev.vikey.devlog

import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import dev.vikey.devlog.presentation.cli.DevlogCommand
import dev.vikey.devlog.presentation.cli.InitCommand
import dev.vikey.devlog.presentation.cli.ReportCommand
import dev.vikey.devlog.presentation.cli.StandupCommand
import dev.vikey.devlog.presentation.cli.TodayCommand
import dev.vikey.devlog.presentation.cli.WeekCommand
import dev.vikey.devlog.presentation.cli.YesterdayCommand
import dev.vikey.devlog.presentation.di.dataModule
import dev.vikey.devlog.presentation.di.domainModule
import dev.vikey.devlog.presentation.di.presentationModule
import org.koin.core.context.startKoin

fun main(args: Array<String>) {
    startKoin {
        modules(dataModule, domainModule, presentationModule)
    }
    DevlogCommand()
        .subcommands(
            TodayCommand(),
            WeekCommand(),
            StandupCommand(),
            YesterdayCommand(),
            ReportCommand(),
            InitCommand(),
        )
        .main(args)
}
