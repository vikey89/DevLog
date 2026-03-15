# Contributing to DevLog

Thanks for your interest in contributing! DevLog uses Clean Architecture, so most contributions are self-contained in a single layer.

## Setup

```bash
git clone https://github.com/vikey89/devlog.git
cd devlog
./gradlew build    # requires JDK 21 (Temurin recommended)
```

> **Note:** If Gradle can't find JDK 21, set `JAVA_HOME` to your JDK 21 installation path.

## Quality checks

Every PR must pass:

```bash
./gradlew detekt   # static analysis — zero issues
./gradlew test     # all tests green
```

## Adding a new LLM provider

1. Create `src/main/kotlin/dev/vikey/devlog/data/llm/YourSource.kt`
2. Implement the `LlmSource` interface:

```kotlin
class YourSource(
    private val client: HttpClient,
    private val apiKey: Lazy<String>,
    private val model: String,
) : LlmSource {
    override suspend fun generate(prompt: String, system: String): Result<String> = runCatching {
        // HTTP call with Ktor Client
    }
}
```

3. Add a value to the `Provider` enum in `domain/model/Provider.kt`
4. Register it in `ProviderRegistry.kt`

That's it. One file + one line.

## Adding a new output renderer

1. Create `src/main/kotlin/dev/vikey/devlog/presentation/renderer/YourRenderer.kt`
2. Implement the `Renderer` interface:

```kotlin
class YourRenderer : Renderer {
    override suspend fun render(report: DevlogReport) {
        // format and print the report
    }
}
```

3. Register it in `RendererFactory.kt`:

```kotlin
"your-format" -> YourRenderer()
```

## Adding a new data source

1. Define the interface in `domain/repository/`
2. Implement in `data/` (e.g., `data/jira/JiraDataSource.kt`)
3. Create the repository implementation in `data/repository/`
4. Wire it in `presentation/di/AppModule.kt`

## Architecture rules

- **Domain layer** has minimal dependencies (Kotlin stdlib + coroutines-core + kotlinx-datetime)
- **Data layer** implements domain interfaces
- **Presentation layer** depends on domain use cases
- Use `data class` for models, `interface` for contracts
- Use fakes (not mocks) for testing — put them in `test/fakes/`
- Every use case must have tests

## Commit style

```
type(scope): short description

# Examples:
feat(llm): add Mistral provider
fix(git): handle repos with no commits
test(daily): add cache invalidation test
```
