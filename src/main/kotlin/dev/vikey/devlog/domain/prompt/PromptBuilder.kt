package dev.vikey.devlog.domain.prompt

import dev.vikey.devlog.domain.model.RepoActivity
import dev.vikey.devlog.domain.model.ReportType

const val SYSTEM_PROMPT = "You are a developer writing a concise development log. " +
    "Write in {language}. Be factual, use bullet points where appropriate, " +
    "and focus on what was accomplished. Keep it brief."

class PromptBuilder {
    fun build(type: ReportType, activities: List<RepoActivity>, language: String): String =
        buildString {
            appendLine(headerFor(type, language))
            appendLine()
            appendActivities(activities)
        }

    private fun headerFor(type: ReportType, language: String): String = when (type) {
        is ReportType.Daily, is ReportType.Yesterday -> {
            "Generate a daily development log from the following git activity.\n" +
                "Language: $language"
        }
        is ReportType.Weekly -> {
            "Generate a weekly development summary from the following git activity.\n" +
                "Group by theme or project. Language: $language"
        }
        is ReportType.Standup -> {
            "Generate a standup update (what I did, what I'll do, blockers) " +
                "from the following git activity.\n" +
                "Keep it very short. Language: $language"
        }
        is ReportType.Range -> {
            "Generate a development report for the period ${type.from} to ${type.to} " +
                "from the following git activity.\n" +
                "Language: $language"
        }
    }

    private fun StringBuilder.appendActivities(activities: List<RepoActivity>) {
        activities.forEach { activity ->
            appendLine("=== ${activity.name} (branch: ${activity.branch}) ===")
            activity.commits.forEach { commit ->
                appendLine("${commit.hash} | ${commit.message}")
            }
            appendLine("+${activity.totalInsertions} -${activity.totalDeletions} | ${activity.totalFilesChanged} files")
            appendLine()
        }
    }
}
