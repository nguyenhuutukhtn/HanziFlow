package com.hanziflow.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.verify.assertEmpty
import org.junit.Test

/**
 * Enforces the dependency rules from docs/app_architecture.md "Code architecture (Android)",
 * verbatim: ui -> domain -> data, ui -> playback (PlayerConnection), everything may use
 * shared; data and playback never import ui; Composables never touch Room/OkHttp/MediaController.
 */
class LayeringTest {
    private val scope = Konsist.scopeFromProject()

    @Test
    fun `data never imports ui`() {
        scope.files
            .withPackage("com.hanziflow.data..")
            .filter { file -> file.imports.any { it.name.startsWith("com.hanziflow.ui") } }
            .assertEmpty()
    }

    @Test
    fun `playback never imports ui`() {
        scope.files
            .withPackage("com.hanziflow.playback..")
            .filter { file -> file.imports.any { it.name.startsWith("com.hanziflow.ui") } }
            .assertEmpty()
    }

    @Test
    fun `di never imports ui`() {
        scope.files
            .withPackage("com.hanziflow.di..")
            .filter { file -> file.imports.any { it.name.startsWith("com.hanziflow.ui") } }
            .assertEmpty()
    }

    @Test
    fun `composables never import Room, OkHttp, or MediaController-MediaSession-ExoPlayer types directly`() {
        val forbidden = listOf(
            "androidx.room",
            "okhttp3",
            "androidx.media3.session.MediaController",
            "androidx.media3.session.MediaSession",
            "androidx.media3.exoplayer",
        )
        scope.files
            .withPackage("com.hanziflow.ui..")
            .filter { file -> file.imports.any { import -> forbidden.any { import.name.startsWith(it) } } }
            .assertEmpty()
    }

    @Test
    fun `only PlayerConnection and PlaybackService touch MediaController or MediaSession`() {
        val allowed = setOf("PlayerConnection", "PlayerConnectionImpl", "PlaybackService")
        scope.files
            .filter { file ->
                val usesMediaSessionTypes = file.imports.any {
                    it.name.startsWith("androidx.media3.session.MediaController") ||
                        it.name.startsWith("androidx.media3.session.MediaSession")
                }
                usesMediaSessionTypes && file.name.removeSuffix(".kt") !in allowed
            }
            .assertEmpty()
    }

    @Test
    fun `ui theme stays pure design tokens`() {
        val forbidden = listOf("com.hanziflow.data", "com.hanziflow.playback", "androidx.media3")
        scope.files
            .withPackage("com.hanziflow.ui.theme..")
            .filter { file -> file.imports.any { import -> forbidden.any { import.name.startsWith(it) } } }
            .assertEmpty()
    }

    @Test
    fun `ui navigation wires state and callbacks without touching playback or data sources`() {
        val forbidden = listOf("com.hanziflow.playback", "androidx.media3", "androidx.room", "okhttp3")
        scope.files
            .withPackage("com.hanziflow.ui.navigation..")
            .filter { file -> file.imports.any { import -> forbidden.any { import.name.startsWith(it) } } }
            .assertEmpty()
    }
}
