package dev.vikey.devlog.presentation.di

import com.github.ajalt.mordant.terminal.Terminal
import dev.vikey.devlog.data.cache.FileCacheSource
import dev.vikey.devlog.data.git.GitCliDataSource
import dev.vikey.devlog.data.git.GitDataSource
import dev.vikey.devlog.data.ConfigLoader
import dev.vikey.devlog.data.llm.ProviderRegistry
import dev.vikey.devlog.data.repository.CacheRepositoryImpl
import dev.vikey.devlog.data.repository.GitRepositoryImpl
import dev.vikey.devlog.data.repository.LlmRepositoryImpl
import dev.vikey.devlog.domain.model.AppConfig
import dev.vikey.devlog.domain.prompt.PromptBuilder
import dev.vikey.devlog.domain.repository.CacheRepository
import dev.vikey.devlog.domain.repository.GitRepository
import dev.vikey.devlog.domain.repository.LlmRepository
import dev.vikey.devlog.domain.validator.ConfigValidator
import dev.vikey.devlog.domain.usecase.GenerateReportUseCase
import dev.vikey.devlog.presentation.renderer.Renderer
import dev.vikey.devlog.presentation.renderer.RendererFactory
import dev.vikey.devlog.presentation.renderer.TerminalRenderer
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

private const val REQUEST_TIMEOUT_MS = 120_000L

val dataModule = module {
    single { ConfigLoader() }
    single {
        HttpClient(CIO) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(io.ktor.client.plugins.HttpTimeout) {
                requestTimeoutMillis = REQUEST_TIMEOUT_MS
            }
            install(io.ktor.client.plugins.DefaultRequest) {
                contentType(io.ktor.http.ContentType.Application.Json)
            }
        }
    }
    single { ProviderRegistry(get(), get()) }
    single<GitDataSource> { GitCliDataSource() }
    single { FileCacheSource() }
    single<GitRepository> { GitRepositoryImpl(get()) }
    single<CacheRepository> { CacheRepositoryImpl(get()) }
    factory<LlmRepository> { (config: AppConfig) ->
        LlmRepositoryImpl(get<ProviderRegistry>().getSource(config))
    }
}

val domainModule = module {
    single { ConfigValidator() }
    single { PromptBuilder() }
    factory { (config: AppConfig) ->
        GenerateReportUseCase(get(), get { parametersOf(config) }, get(), get())
    }
}

val presentationModule = module {
    single { Terminal() }
    single { RendererFactory() }
    single<Renderer> { TerminalRenderer(get()) }
}
