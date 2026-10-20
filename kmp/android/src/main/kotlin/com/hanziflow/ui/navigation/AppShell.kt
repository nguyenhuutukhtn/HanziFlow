package com.hanziflow.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hanziflow.R
import com.hanziflow.data.LessonEntity
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.ui.components.MiniPlayer
import com.hanziflow.ui.components.trackDisplayName
import com.hanziflow.ui.library.LibraryScreen
import com.hanziflow.ui.library.LibraryUiState
import com.hanziflow.ui.player.NowPlayingScreen
import com.hanziflow.ui.player.PlayerUiState
import com.hanziflow.ui.review.ReviewScreen
import com.hanziflow.ui.settings.SettingsScreen

object Routes {
    const val LEARN = "learn"
    const val REVIEW = "review"
    const val SETTINGS = "settings"
    const val NOW_PLAYING = "now_playing"
}

/** Bottom-nav destinations; Review and Settings are G2b/G2c placeholders in this pass. */
enum class Tab(
    val route: String,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    val testTag: String,
) {
    LEARN(Routes.LEARN, R.string.nav_learn, R.drawable.ic_nav_learn, "nav_learn"),
    REVIEW(Routes.REVIEW, R.string.nav_review, R.drawable.ic_nav_review, "nav_review"),
    SETTINGS(Routes.SETTINGS, R.string.nav_settings, R.drawable.ic_nav_settings, "nav_settings"),
}

/**
 * Stateless shell: bottom navigation, the persistent mini-player, and Now Playing as
 * a full-screen route. Playback lives in the activity-scoped PlayerViewModel passed in
 * from [com.hanziflow.MainActivity], so switching tabs never touches the player.
 */
@Composable
fun AppShell(
    libraryState: LibraryUiState,
    playerState: PlayerUiState,
    onEditBaseUrl: (String) -> Unit,
    onSync: () -> Unit,
    onDownload: (String) -> Unit,
    onCancel: (String) -> Unit,
    onDelete: (String) -> Unit,
    onPlayLesson: (LessonEntity) -> Unit,
    onToggle: () -> Unit,
    onReplayLine: () -> Unit,
    onSwitchTrack: (String) -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekBack: () -> Unit,
    onSeekForward: () -> Unit,
    onSpeed: (Float) -> Unit,
    onCancelLoop: () -> Unit,
    onSeekLine: (String) -> Unit,
    onToggleLoop: (String) -> Unit,
    onLookup: (String) -> Unit,
    onViewModeChange: (TranscriptViewMode) -> Unit,
    onVietnameseToggle: (Boolean) -> Unit,
    onPlayLookupClip: () -> Unit,
    onDismissLookup: () -> Unit,
    modifier: Modifier = Modifier,
    onChineseOnlyToggle: (Boolean) -> Unit = {},
    onLoopSection: () -> Unit = {},
    onStopSectionLoopAfterPass: () -> Unit = {},
    onNextSection: () -> Unit = {},
    onSeekToSection: (String) -> Unit = {},
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val onTabRoute = currentRoute == Routes.LEARN ||
        currentRoute == Routes.REVIEW ||
        currentRoute == Routes.SETTINGS

    fun openTab(tab: Tab) {
        navController.navigate(tab.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (onTabRoute) {
                Column {
                    if (playerState.lessonId != null) {
                        MiniPlayer(
                            title = playerState.lessonTitle ?: playerState.lessonId,
                            trackLabel = trackDisplayName(playerState.track),
                            playing = playerState.playing,
                            positionMs = playerState.positionMs,
                            durationMs = playerState.durationMs,
                            onToggle = onToggle,
                            onReplayLine = onReplayLine,
                            onExpand = { navController.navigate(Routes.NOW_PLAYING) },
                            sectionLoop = playerState.sectionLoop,
                            onStopSectionLoopAfterPass = onStopSectionLoopAfterPass,
                            onNextSection = onNextSection,
                        )
                    }
                    NavigationBar {
                        Tab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = currentRoute == tab.route,
                                onClick = { openTab(tab) },
                                icon = {
                                    Icon(
                                        painter = painterResource(tab.icon),
                                        contentDescription = null,
                                    )
                                },
                                label = { Text(stringResource(tab.label)) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                                modifier = Modifier.testTag(tab.testTag),
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.LEARN,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.LEARN) {
                LibraryScreen(
                    state = libraryState,
                    onEditBaseUrl = onEditBaseUrl,
                    onSync = onSync,
                    onDownload = onDownload,
                    onCancel = onCancel,
                    onDelete = onDelete,
                    onPlay = { lesson ->
                        onPlayLesson(lesson)
                        navController.navigate(Routes.NOW_PLAYING)
                    },
                )
            }
            composable(Routes.REVIEW) { ReviewScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
            composable(Routes.NOW_PLAYING) {
                NowPlayingScreen(
                    state = playerState,
                    onCollapse = { navController.popBackStack() },
                    onSwitchTrack = onSwitchTrack,
                    onSeekTo = onSeekTo,
                    onSeekBack = onSeekBack,
                    onToggle = onToggle,
                    onSeekForward = onSeekForward,
                    onSpeed = onSpeed,
                    onCancelLoop = onCancelLoop,
                    onSeekLine = onSeekLine,
                    onToggleLoop = onToggleLoop,
                    onLookup = onLookup,
                    onViewModeChange = onViewModeChange,
                    onVietnameseToggle = onVietnameseToggle,
                    onPlayLookupClip = onPlayLookupClip,
                    onDismissLookup = onDismissLookup,
                    onChineseOnlyToggle = onChineseOnlyToggle,
                    onLoopSection = onLoopSection,
                    onStopSectionLoopAfterPass = onStopSectionLoopAfterPass,
                    onNextSection = onNextSection,
                    onSeekToSection = onSeekToSection,
                )
            }
        }
    }
}
