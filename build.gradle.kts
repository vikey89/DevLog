plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.graalvm.native)
    alias(libs.plugins.shadow)
    application
}

group = "dev.vikey"
version = "0.1.0"

application {
    mainClass.set("dev.vikey.devlog.MainKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // CLI
    implementation(libs.clikt)
    implementation(libs.mordant)

    // HTTP
    implementation(libs.bundles.ktor.client)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // DI
    implementation(libs.koin.core)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Logging (suppress SLF4J warnings from Ktor)
    implementation(libs.slf4j.nop)

    // Config
    implementation(libs.kaml)

    // Date/Time
    implementation(libs.kotlinx.datetime)

    // Test
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.koin.test)
}

detekt {
    config.setFrom("detekt.yml")
    buildUponDefaultConfig = true
}

graalvmNative {
    binaries {
        named("main") {
            mainClass.set("dev.vikey.devlog.MainKt")
            imageName.set("devlog")
            buildArgs.add("--no-fallback")
        }
    }
}

val generateVersion by tasks.registering {
    val outputDir = layout.buildDirectory.dir("generated/version")
    val ver = project.version.toString()
    outputs.dir(outputDir)
    doLast {
        val file = outputDir.get().file("dev/vikey/devlog/Version.kt").asFile
        file.parentFile.mkdirs()
        file.writeText(
            "package dev.vikey.devlog\n\nconst val APP_VERSION = \"$ver\"\n"
        )
    }
}

sourceSets {
    main {
        kotlin.srcDir(generateVersion)
    }
}

tasks.register("printVersion") {
    doLast { print(project.version) }
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("all")
    mergeServiceFiles()
    manifest {
        attributes("Main-Class" to "dev.vikey.devlog.MainKt")
    }
}
