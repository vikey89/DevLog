package dev.vikey.devlog.domain.model

enum class Provider(val id: String) {
    ANTHROPIC("anthropic"),
    OPENAI("openai"),
    GEMINI("gemini"),
    ;

    companion object {
        fun fromId(id: String): Provider? = entries.find { it.id == id }
    }
}
