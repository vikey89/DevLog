package dev.vikey.devlog.domain.validator

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class ConfigValidatorTest {

    private val validator = ConfigValidator()

    @Test
    fun `accepts standard env var names`() {
        validator.isValidEnvVarName("ANTHROPIC_API_KEY") shouldBe true
        validator.isValidEnvVarName("OPENAI_API_KEY") shouldBe true
        validator.isValidEnvVarName("MY_VAR") shouldBe true
    }

    @Test
    fun `accepts names starting with underscore`() {
        validator.isValidEnvVarName("_PRIVATE") shouldBe true
    }

    @Test
    fun `accepts lowercase names`() {
        validator.isValidEnvVarName("api_key") shouldBe true
    }

    @Test
    fun `accepts single character names`() {
        validator.isValidEnvVarName("A") shouldBe true
        validator.isValidEnvVarName("_") shouldBe true
    }

    @Test
    fun `rejects names starting with a digit`() {
        validator.isValidEnvVarName("1INVALID") shouldBe false
    }

    @Test
    fun `rejects names with special characters`() {
        validator.isValidEnvVarName("MY-KEY") shouldBe false
        validator.isValidEnvVarName("MY.KEY") shouldBe false
        validator.isValidEnvVarName("MY KEY") shouldBe false
    }

    @Test
    fun `rejects empty string`() {
        validator.isValidEnvVarName("") shouldBe false
    }

    @Test
    fun `rejects raw API key values`() {
        validator.isValidEnvVarName("sk-ant-api03-abc123") shouldBe false
        validator.isValidEnvVarName("sk-proj-abc123") shouldBe false
    }
}
