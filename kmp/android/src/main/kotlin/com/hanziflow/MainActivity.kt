package com.hanziflow

import android.Manifest
import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.hanziflow.data.LessonEntity
import com.hanziflow.playback.PlaybackService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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

data class PlayerUi(
    val connected: Boolean = false,
    val playing: Boolean = false,
    val lessonId: String? = null,
    val track: String = "guided",
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val speed: Float = 1f,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application as HanziFlowApplication
    private val repository = app.repository
    val lessons = repository.lessons.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    var baseUrl by mutableStateOf("")
        private set
    var message by mutableStateOf<String?>(null)
        private set
    var playerUi by mutableStateOf(PlayerUi())
        private set

    private val controllerFuture: ListenableFuture<MediaController>
    private var controller: MediaController? = null

    init {
        viewModelScope.launch { baseUrl = repository.baseUrl() }
        val token = SessionToken(application, ComponentName(application, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(application, token).buildAsync()
        controllerFuture.addListener({
            runCatching { controllerFuture.get() }.onSuccess { connected ->
                controller = connected
                connected.addListener(object : Player.Listener {
                    override fun onEvents(player: Player, events: Player.Events) = refreshPlayer()
                })
                refreshPlayer()
            }.onFailure { message = it.message }
        }, ContextCompat.getMainExecutor(application))
        viewModelScope.launch {
            while (true) {
                delay(500)
                refreshPlayer()
            }
        }
    }

    fun editBaseUrl(value: String) { baseUrl = value }

    fun sync() = viewModelScope.launch {
        message = "Syncing catalog…"
        runCatching {
            repository.setBaseUrl(baseUrl)
            repository.syncCatalog()
        }.onSuccess { message = "Catalog updated" }
            .onFailure { message = it.message ?: "Catalog sync failed" }
    }

    fun download(id: String) = repository.download(id)
    fun cancel(id: String) = repository.cancel(id)
    fun delete(id: String) = viewModelScope.launch { repository.delete(id) }

    fun play(lesson: LessonEntity, track: String = "guided") = viewModelScope.launch {
        val file = repository.trackFile(lesson.id, track)
        if (file == null) {
            message = "Download this lesson first"
            return@launch
        }
        controller?.apply {
            setMediaItem(PlaybackService.mediaItem(lesson.id, track, file.toURI().toString(), lesson.title))
            prepare()
            play()
        } ?: run { message = "Player is still connecting" }
    }

    fun toggle() { controller?.let { if (it.isPlaying) it.pause() else it.play() } }
    fun seekBack() { controller?.seekBack() }
    fun seekForward() { controller?.seekForward() }
    fun seekTo(position: Long) { controller?.seekTo(position) }
    fun speed(value: Float) { controller?.setPlaybackSpeed(value) }

    fun switchTrack(track: String) = viewModelScope.launch {
        val id = playerUi.lessonId ?: return@launch
        val lesson = lessons.value.firstOrNull { it.id == id } ?: return@launch
        play(lesson, track)
    }

    private fun refreshPlayer() {
        val current = controller ?: return
        val parts = current.currentMediaItem?.mediaId?.split('|', limit = 2).orEmpty()
        playerUi = PlayerUi(
            connected = true,
            playing = current.isPlaying,
            lessonId = parts.getOrNull(0),
            track = parts.getOrNull(1) ?: "guided",
            positionMs = current.currentPosition.coerceAtLeast(0),
            durationMs = current.duration.coerceAtLeast(0),
            speed = current.playbackParameters.speed,
        )
    }

    override fun onCleared() {
        MediaController.releaseFuture(controllerFuture)
        super.onCleared()
    }
}

@Composable
fun HanziFlowScreen(vm: MainViewModel = viewModel()) {
    val lessons by vm.lessons.collectAsStateWithLifecycle()
    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("HanziFlow", style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = vm.baseUrl,
                    onValueChange = vm::editBaseUrl,
                    label = { Text("Catalog base URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = vm::sync) { Text("Sync catalog") }
                vm.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            }
            if (vm.playerUi.lessonId != null) item { PlayerCard(vm) }
            item { Text("Library", style = MaterialTheme.typography.titleLarge) }
            items(lessons, key = { it.id }) { lesson -> LessonCard(lesson, vm) }
        }
    }
}

@Composable
private fun LessonCard(lesson: LessonEntity, vm: MainViewModel) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(lesson.title, style = MaterialTheme.typography.titleMedium)
            Text("${lesson.level} · v${lesson.remoteVersion} · ${lesson.downloadState}")
            lesson.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                when (lesson.downloadState) {
                    "DOWNLOADING" -> OutlinedButton(onClick = { vm.cancel(lesson.id) }) { Text("Cancel") }
                    "DOWNLOADED" -> {
                        Button(onClick = { vm.play(lesson) }) { Text("Play") }
                        OutlinedButton(onClick = { vm.delete(lesson.id) }) { Text("Delete") }
                    }
                    "UPDATE_AVAILABLE" -> {
                        Button(onClick = { vm.play(lesson) }) { Text("Play old") }
                        Button(onClick = { vm.download(lesson.id) }) { Text("Update") }
                    }
                    else -> Button(onClick = { vm.download(lesson.id) }) {
                        Text(if (lesson.downloadState == "FAILED" || lesson.downloadState == "CANCELLED") "Retry" else "Download")
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerCard(vm: MainViewModel) {
    val state = vm.playerUi
    val lesson = vm.lessons.collectAsStateWithLifecycle().value.firstOrNull { it.id == state.lessonId }
    var practiceAvailable by remember(lesson?.catalogJson) { mutableStateOf(false) }
    LaunchedEffect(lesson?.catalogJson) {
        practiceAvailable = lesson?.let { vm.getApplication<HanziFlowApplication>().repository.catalogLesson(it.id)?.tracks?.practice != null } == true
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Now playing: ${lesson?.title ?: state.lessonId}", style = MaterialTheme.typography.titleMedium)
            if (practiceAvailable) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = state.track == "guided", onClick = { vm.switchTrack("guided") }, label = { Text("Guided") })
                FilterChip(selected = state.track == "practice", onClick = { vm.switchTrack("practice") }, label = { Text("Practice") })
            }
            Slider(
                value = state.positionMs.toFloat().coerceAtMost(state.durationMs.coerceAtLeast(1).toFloat()),
                onValueChange = { vm.seekTo(it.toLong()) },
                valueRange = 0f..state.durationMs.coerceAtLeast(1).toFloat(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = vm::seekBack) { Text("−10 s") }
                Button(onClick = vm::toggle) { Text(if (state.playing) "Pause" else "Play") }
                OutlinedButton(onClick = vm::seekForward) { Text("+10 s") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(0.8f, 0.9f, 1f, 1.1f).forEach { speed ->
                    FilterChip(
                        selected = state.speed == speed,
                        onClick = { vm.speed(speed) },
                        label = { Text("${speed}×") },
                    )
                }
            }
        }
    }
}
