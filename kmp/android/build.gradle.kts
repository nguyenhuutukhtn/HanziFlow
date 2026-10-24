import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.roborazzi)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}
val supabaseUrl = localProperties.getProperty("supabase.url")
    ?.trim()
    ?.trimEnd('/')
    ?.takeIf { it.isNotEmpty() }
    ?: error("Add supabase.url=https://<project-ref>.supabase.co to kmp/local.properties")
fun llmProp(key: String): String =
    (localProperties.getProperty(key) ?: "").trim().replace("\\", "").replace("\"", "")
val llmBaseUrl = llmProp("llm.base_url")
val llmModel = llmProp("llm.model")
val llmApiKey = llmProp("llm.api_key")
val contentBaseUrl = "$supabaseUrl/storage/v1/object/public/course/"

android {
    namespace = "com.hanziflow"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.hanziflow"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "CONTENT_BASE_URL", "\"$contentBaseUrl\"")
        // Optional AI explain config (docs/tasks/X2.md); blank means the feature is off.
        buildConfigField("String", "LLM_BASE_URL", "\"$llmBaseUrl\"")
        buildConfigField("String", "LLM_MODEL", "\"$llmModel\"")
        buildConfigField("String", "LLM_API_KEY", "\"$llmApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    packaging.resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"

    testOptions.unitTests.isIncludeAndroidResources = true
    sourceSets.getByName("main").assets.srcDir(layout.buildDirectory.dir("generated/promptAssets").get().asFile)
}

// One source of truth: repo-root prompts/ is copied into the APK assets as assets/prompts/.
val copyPromptAssets = tasks.register<Copy>("copyPromptAssets") {
    from(rootProject.file("../prompts")) { include("*.md") }
    into(layout.buildDirectory.dir("generated/promptAssets/prompts"))
}
tasks.named("preBuild") { dependsOn(copyPromptAssets) }

roborazzi {
    // Until the user approves the screens, every capture stays in the git-ignored
    // build directory. Move this to src/test/screenshots when goldens are adopted.
    outputDir.set(layout.buildDirectory.dir("outputs/roborazzi"))
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.work.runtime)
    implementation(libs.okhttp)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.markdown.renderer.m3)
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.konsist)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
