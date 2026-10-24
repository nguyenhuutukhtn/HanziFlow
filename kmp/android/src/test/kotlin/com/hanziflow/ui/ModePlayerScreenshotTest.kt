package com.hanziflow.ui

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import com.hanziflow.shared.modes.GapState
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.ui.player.PlayerUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w411dp-h891dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ModePlayerScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private fun modeState(track: String, gap: GapState? = null): PlayerUiState =
        nowPlayingState(TranscriptViewMode.HANZI_WITH_PINYIN).copy(
            lessonId = "lesson_002",
            lessonTitle = "Gọi điện thoại · Nhầm tòa nhà",
            track = track,
            lessonFormat = "modes_v1",
            activeGap = gap,
        )

    @Test fun phan_xa_gap_dark() = compose.captureScreen("mode_phan_xa_gap_dark") {
        NowPlayingFrame(modeState("phan_xa", GapState("answer", 2_294, 3_420, "phan_xa_q2")))
    }

    @Test fun phan_xa_gap_light() = compose.captureScreen("mode_phan_xa_gap_light") {
        NowPlayingFrame(modeState("phan_xa", GapState("answer", 2_294, 3_420, "phan_xa_q2")), false)
    }

    @Test fun noi_gap_dark() = compose.captureScreen("mode_noi_gap_dark") {
        NowPlayingFrame(modeState("noi", GapState("repeat", 2_800, 4_200, "noi_l1")))
    }

    @Test fun noi_gap_light() = compose.captureScreen("mode_noi_gap_light") {
        NowPlayingFrame(modeState("noi", GapState("repeat", 2_800, 4_200, "noi_l1")), false)
    }

    @Test fun noi_repeat_each_dark() = compose.captureScreen("mode_noi_repeat_each_dark") {
        NowPlayingFrame(modeState("noi").copy(repeatEachEnabled = true))
    }

    @Test fun noi_repeat_each_light() = compose.captureScreen("mode_noi_repeat_each_light") {
        NowPlayingFrame(modeState("noi").copy(repeatEachEnabled = true), false)
    }

    @Test fun nghe_hidden_dark() = compose.captureScreen("mode_nghe_hidden_dark") {
        NowPlayingFrame(modeState("nghe").let { it.copy(transcript = it.transcript.copy(lines = emptyList())) })
    }

    @Test fun nghe_hidden_light() = compose.captureScreen("mode_nghe_hidden_light") {
        NowPlayingFrame(modeState("nghe").let { it.copy(transcript = it.transcript.copy(lines = emptyList())) }, false)
    }

    @Test fun nghe_revealed_dark() = compose.captureScreen("mode_nghe_revealed_dark") {
        NowPlayingFrame(modeState("nghe").let { it.copy(transcript = it.transcript.copy(lines = sampleLines.take(1))) })
    }

    @Test fun nghe_revealed_light() = compose.captureScreen("mode_nghe_revealed_light") {
        NowPlayingFrame(modeState("nghe").let { it.copy(transcript = it.transcript.copy(lines = sampleLines.take(1))) }, false)
    }
}
