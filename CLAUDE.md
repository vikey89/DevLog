# DevLog — Project Rules

## Architecture
- Clean Architecture: domain/ -> data/ -> presentation/
- Domain layer has minimal dependencies (kotlin stdlib + coroutines-core + kotlinx-datetime)
- All repository interfaces are in domain/repository/
- Implementations in data/repository/
- DI via Koin modules in presentation/di/AppModule.kt
- Koin startKoin {} in Main.kt
- Use parametersOf(config) for config-dependent dependencies
- Commands implement KoinComponent to get() dependencies

## Code Quality
- Kotlin 2.1+ with strict null safety
- Run `./gradlew detekt` — must pass with zero issues
- Run `./gradlew test` — all tests must pass
- Every use case must have tests with fakes, not mocks

## Conventions
- data class (immutable) for domain models
- interface for repository contracts
- Result<String> for LLM output
- suspend functions for async operations
- No SDK wrappers — only Ktor Client for HTTP calls
- Prompts in domain/prompt/PromptBuilder.kt, never hardcoded in use cases
- No exceptions in domain — use Result<T> and nullable error fields

## Test Rules
- Fakes in test/fakes/, not Mockito/MockK
- Test behavior, not implementation
- Each use case test file tests one use case
- Use runTest {} for coroutine tests
- Use Kotest assertions for test verification

## Build
- JAVA_HOME must be set to JDK 21: export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home
- Always run Gradle with: export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home && ./gradlew <task>
