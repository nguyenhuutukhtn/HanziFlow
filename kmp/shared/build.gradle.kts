import org.jetbrains.kotlin.gradle.dsl.JvmTarget

val fixtureRoot = layout.projectDirectory.dir("src/commonTest/resources/contract")
val fixtureFiles = listOf(
    "catalog.json",
    "lesson.json",
    "reference.json",
    "items.json",
    "characters.json",
    "strokes.json",
    "timeline_guided.json",
    "timeline_guided_zh.json",
)
val generatedFixtureDir = layout.buildDirectory.dir("generated/fixtureTest/kotlin")
val generateFixtureData = tasks.register("generateFixtureData") {
    inputs.files(fixtureFiles.map { fixtureRoot.file(it) })
    val output = generatedFixtureDir.map { it.file("com/hanziflow/shared/FixtureData.kt") }
    outputs.file(output)
    doLast {
        val entries = fixtureFiles.joinToString(",\n") { relative ->
            val json = fixtureRoot.file(relative).asFile.readText().replace("$", "${'$'}{'$'}")
            "    \"$relative\" to \"\"\"$json\"\"\""
        }
        val target = output.get().asFile
        target.parentFile.mkdirs()
        target.writeText(
            """package com.hanziflow.shared

internal val FIXTURE_JSON = mapOf(
$entries,
)
"""
        )
    }
}

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "com.hanziflow.shared"
        compileSdk = 37
        minSdk = 26
        compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
        withHostTestBuilder {}
    }
    jvm()
    js {
        browser()
        nodejs()
        binaries.library()
        generateTypeScriptDefinitions()
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        commonTest {
            kotlin.srcDir(generatedFixtureDir)
        }
    }
}

tasks.matching { it.name.contains("Test") || it.name.contains("AndroidHostTest") }.configureEach {
    dependsOn(generateFixtureData)
}
