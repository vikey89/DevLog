package dev.vikey.devlog.domain.model

sealed interface ReportType {
    data object Daily : ReportType
    data object Yesterday : ReportType
    data object Weekly : ReportType
    data object Standup : ReportType
    data class Range(val from: String, val to: String) : ReportType
}
