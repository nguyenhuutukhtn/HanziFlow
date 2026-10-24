package com.hanziflow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hanziflow.ui.app.AppShellViewModel
import com.hanziflow.ui.library.LibraryViewModel
import com.hanziflow.ui.navigation.AppShell
import com.hanziflow.ui.modes.ModePickerViewModel
import com.hanziflow.ui.notes.NotesViewModel
import com.hanziflow.ui.player.PlayerViewModel
import com.hanziflow.ui.theme.HanziFlowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val notifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent {
            val shellViewModel: AppShellViewModel = hiltViewModel()
            val shellState by shellViewModel.uiState.collectAsStateWithLifecycle()
            HanziFlowTheme(darkTheme = shellState.darkTheme) {
                Surface(Modifier.fillMaxSize()) { HanziFlowApp() }
            }
        }
    }
}

/**
 * Thin composition root: wires the activity-scoped screen ViewModels (via Hilt) to
 * the stateless [AppShell]. It owns no business logic, so the shell can be rendered
 * with fake states in Compose tests.
 */
@Composable
fun HanziFlowApp(
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    modePickerViewModel: ModePickerViewModel = hiltViewModel(),
    notesViewModel: NotesViewModel = hiltViewModel(),
) {
    val libraryState by libraryViewModel.uiState.collectAsStateWithLifecycle()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val notesState by notesViewModel.uiState.collectAsStateWithLifecycle()
    val modePickerState by modePickerViewModel.uiState.collectAsStateWithLifecycle()

    AppShell(
        libraryState = libraryState,
        playerState = playerState,
        onEditBaseUrl = libraryViewModel::editBaseUrl,
        onSync = libraryViewModel::sync,
        onDownload = libraryViewModel::download,
        onCancel = libraryViewModel::cancel,
        onDelete = libraryViewModel::delete,
        onPlayLesson = { lesson -> playerViewModel.play(lesson) },
        onToggle = playerViewModel::toggle,
        onReplayLine = playerViewModel::replayCurrentLine,
        onSwitchTrack = playerViewModel::switchTrack,
        onSeekTo = playerViewModel::seekTo,
        onSeekBack = playerViewModel::seekBack,
        onSeekForward = playerViewModel::seekForward,
        onSpeed = playerViewModel::speed,
        onCancelLoop = playerViewModel::cancelLoop,
        onSeekLine = playerViewModel::seekToLine,
        onToggleLoop = playerViewModel::toggleLoopForLine,
        onLookup = playerViewModel::lookupToken,
        onViewModeChange = playerViewModel::setTranscriptViewMode,
        onVietnameseToggle = playerViewModel::setTranscriptVietnamese,
        onPlayLookupClip = playerViewModel::playLookupClip,
        onDismissLookup = playerViewModel::dismissLookup,
        onChineseOnlyToggle = playerViewModel::setChineseOnly,
        onLoopSection = playerViewModel::loopCurrentSection,
        onStopSectionLoopAfterPass = playerViewModel::stopSectionLoopAfterPass,
        onNextSection = playerViewModel::nextSection,
        onSeekToSection = playerViewModel::seekToSection,
        onCharacter = playerViewModel::lookupCharacter,
        onDismissCharacter = playerViewModel::dismissCharacter,
        onPauseForGap = playerViewModel::pauseForGap,
        onRevealNgheCue = playerViewModel::revealCurrentNgheCue,
        onToggleRepeatEach = playerViewModel::toggleRepeatEach,
        modePickerState = modePickerState,
        notesState = notesState,
        onOpenNotes = { notesViewModel.load(it) },
        onOpenModePicker = { lesson -> modePickerViewModel.load(lesson.id) },
        onPlayMode = { lessonId, track ->
            libraryState.lessons.firstOrNull { it.id == lessonId }?.let { lesson -> playerViewModel.play(lesson, track) }
        },
    )
}
