package dev.vikey.devlog.presentation.renderer

import dev.vikey.devlog.domain.model.DevlogReport

interface Renderer {
    suspend fun render(report: DevlogReport)
}
