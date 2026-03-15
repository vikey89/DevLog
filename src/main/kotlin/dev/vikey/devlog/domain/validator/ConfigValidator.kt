package dev.vikey.devlog.domain.validator

private val ENV_VAR_PATTERN = Regex("^[A-Za-z_][A-Za-z0-9_]*$")

class ConfigValidator {
    fun isValidEnvVarName(name: String): Boolean = ENV_VAR_PATTERN.matches(name)
}
