package com.hanziflow

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hanziflow.ui.library.LibraryScreen
import com.hanziflow.ui.library.LibraryViewModel
import com.hanziflow.ui.player.PlayerCard
import com.hanziflow.ui.player.PlayerUiState
import com.hanziflow.ui.player.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val notifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent { MaterialTheme { Surface(Modifier.fillMaxSize()) { HanziFlowScreen() } } }
    }
}

/**
 * Thin composition root: wires the two screen ViewModels (via Hilt) to the stateless
 * [LibraryScreen] / [PlayerCard] composables. Owns no business logic itself.
 */
@Composable
fun HanziFlowScreen(
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
) {
    val libraryState by libraryViewModel.uiState.collectAsStateWithLifecycle()
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { padding ->
        LibraryScreen(
            state = libraryState,
            onEditBaseUrl = libraryViewModel::editBaseUrl,
            onSync = libraryViewModel::sync,
            onDownload = libraryViewModel::download,
            onCancel = libraryViewModel::cancel,
            onDelete = libraryViewModel::delete,
            onPlay = { lesson -> playerViewModel.play(lesson) },
            modifier = Modifier.fillMaxSize().padding(padding),
            header = playerHeader(playerState, playerViewModel),
        )
    }
}

private fun playerHeader(
    playerState: PlayerUiState,
    playerViewModel: PlayerViewModel,
): LazyListScope.() -> Unit = {
    if (playerState.lessonId != null) item {
        PlayerCard(
            state = playerState,
            onSwitchTrack = playerViewModel::switchTrack,
            onSeekTo = playerViewModel::seekTo,
            onSeekBack = playerViewModel::seekBack,
            onToggle = playerViewModel::toggle,
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
        )
    }
}
