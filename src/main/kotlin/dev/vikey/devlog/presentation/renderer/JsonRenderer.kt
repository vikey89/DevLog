package dev.vikey.devlog.presentation.renderer

import dev.vikey.devlog.data.dto.DevlogReportDto
import dev.vikey.devlog.data.dto.toDto
import dev.vikey.devlog.domain.model.DevlogReport
import kotlinx.serialization.json.Json

class JsonRenderer : Renderer {

    private val json = Json { prettyPrint = true }

    override suspend fun render(report: DevlogReport) {
        println(json.encodeToString(DevlogReportDto.serializer(), report.toDto()))
    }
}
