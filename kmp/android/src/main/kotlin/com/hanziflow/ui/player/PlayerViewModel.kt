package com.hanziflow.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanziflow.data.CourseRepository
import com.hanziflow.data.LessonEntity
import com.hanziflow.data.ProgressRepository
import com.hanziflow.data.SettingsRepository
import com.hanziflow.playback.PlayerConnection
import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.TimelineSection
import com.hanziflow.shared.transcript.SectionNavigation
import com.hanziflow.shared.transcript.TranscriptCues
import com.hanziflow.shared.transcript.TranscriptViewMode
import com.hanziflow.shared.characters.CharacterIndex
import com.hanziflow.shared.modes.ModeInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val settingsRepository: SettingsRepository,
    private val progressRepository: ProgressRepository,
    private val playerConnection: PlayerConnection,
) : ViewModel() {
    constructor(courseRepository: CourseRepository, settingsRepository: SettingsRepository, playerConnection: PlayerConnection) : this(courseRepository, settingsRepository, NoOpProgressRepository, playerConnection)

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val lessons = courseRepository.lessons.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var transcriptLoadJob: Job? = null
    private var practiceLookupJob: Job? = null
    private val progressSaver = PlaybackProgressSaver(progressRepository, viewModelScope)
    internal var backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
    private var loadedTranscript: LoadedTranscript? = null
    init {
        viewModelScope.launch {
            courseRepository.lessons.collect {
                val id = _uiState.value.lessonId ?: return@collect
                val available = courseRepository.trackFile(id, "guided_zh") != null
                _uiState.update { state -> if (state.lessonId == id) state.copy(chineseOnlyAvailable = available) else state }
            }
        }
        viewModelScope.launch {
            playerConnection.state.collect { playback ->
                _uiState.update { state ->
                    val lessonState = state.forPlaybackLesson(playback.lessonId)
                    applyModePresentation(lessonState.copy(
                        connected = playback.connected,
                        playing = playback.playing,
                        lessonId = playback.lessonId,
                        track = playback.track,
                        modeName = ModeInfo.all.firstOrNull { mode -> mode.id == playback.track }?.label,
                        positionMs = playback.positionMs,
                        durationMs = playback.durationMs,
                        speed = playback.speed,
                        sectionLoop = playback.sectionLoop,
                        repeatEachEnabled = playback.repeatEachEnabled,
                        chineseOnly = playback.track == "guided_zh",
                        transcript = state.transcript.copy(loopedLineId = playback.loopedLineId),
                    ), playback.positionMs)
                }
                progressSaver.save(playback)
                val id = playback.lessonId
                if (id != null) {
                    ensureTranscriptLoaded(id, playback.track)
                    ensureLessonMeta(id)
                    ensureCharactersLoaded(id)
                }
            }
        }
    }
    fun play(lesson: LessonEntity, track: String = "guided") = viewModelScope.launch {
        val current = playerConnection.state.value
        if (current.lessonId != null) progressRepository.savePosition(current.lessonId, current.track, current.positionMs)
        val file = courseRepository.trackFile(lesson.id, track)
        if (file == null) {
            _uiState.update { it.copy(message = "Download this lesson first") }
            return@launch
        }
        if (track == "nghe") _uiState.update { applyModePresentation(it.copy(ngheTranscriptVisible = false), it.positionMs) }
        val startPosition = progressRepository.positionMs(lesson.id, track)
        val trackLabel = ModeInfo.all.firstOrNull { it.id == track }?.label
            ?: if (track == "practice") "Luyện nghe" else "Học có hướng dẫn"
        playerConnection.play(lesson.id, track, file.toURI().toString(), lesson.title, trackLabel)
        if (startPosition > 0) playerConnection.seekTo(startPosition)
    }
    fun switchTrack(track: String) = viewModelScope.launch {
        val id = _uiState.value.lessonId ?: return@launch
        val lesson = lessons.value.firstOrNull { it.id == id } ?: return@launch
        play(lesson, track)
    }
    fun toggle() = playerConnection.toggle()
    fun pauseForGap() {
        if (_uiState.value.activeGap != null && _uiState.value.playing) playerConnection.pause()
    }
    fun toggleRepeatEach() {
        val state = _uiState.value
        if (state.lessonFormat != "modes_v1" || state.track !in setOf("noi", "phan_xa")) return
        val enabled = !state.repeatEachEnabled
        playerConnection.setRepeatEach(enabled)
        viewModelScope.launch { settingsRepository.setRepeatEachEnabled(state.lessonId ?: return@launch, state.track, enabled) }
    }

    fun revealCurrentNgheCue() {
        val state = _uiState.value
        if (state.lessonFormat != "modes_v1" || state.track != "nghe") return
        _uiState.update {
            applyModePresentation(it.copy(ngheTranscriptVisible = !it.ngheTranscriptVisible), it.positionMs)
        }
    }
    fun seekBack() = playerConnection.seekBack()
    fun seekForward() = playerConnection.seekForward()
    fun seekTo(position: Long) = playerConnection.seekTo(position)
    fun speed(value: Float) = playerConnection.setSpeed(value)

    fun seekToLine(lineId: String) {
        val transcript = _uiState.value.transcript
        val target = TranscriptCues.seekTargetMs(transcript.cues, lineId, _uiState.value.positionMs) ?: return
        seekTo(target)
    }

    fun seekToSection(sectionId: String) {
        val target = SectionNavigation.sectionStart(_uiState.value.transcript.sections, sectionId) ?: return
        seekTo(target)
    }

    fun replayCurrentLine() {
        val transcript = _uiState.value.transcript
        if (transcript.lessonId == null || transcript.lessonId != _uiState.value.lessonId) return
        val cue = TranscriptCues.activeCue(transcript.cues, _uiState.value.positionMs) ?: return
        playerConnection.seekTo(cue.startMs)
    }

    fun toggleLoopForLine(lineId: String) {
        val transcript = _uiState.value.transcript
        if (transcript.loopedLineId == lineId) {
            cancelLoop()
            return
        }
        val cue = TranscriptCues.activeCue(transcript.cues, _uiState.value.positionMs)
            ?.takeIf { lineId in TranscriptCues.lineIdsFor(it) }
            ?: TranscriptCues.firstCueForLine(transcript.cues, lineId)
            ?: return
        playerConnection.loopSegment(lineId, cue.startMs, cue.endMs)
    }

    fun cancelLoop() = playerConnection.clearLoop()

    fun loopCurrentSection() {
        val section = currentSection() ?: return
        playerConnection.loopSection(section.id, section.title, section.startMs, section.endMs)
    }

    fun stopSectionLoopAfterPass() = playerConnection.stopSectionLoopAfterPass()
    fun nextSection() = playerConnection.nextSection()

    fun currentSection(): TimelineSection? =
        SectionNavigation.sectionAt(_uiState.value.transcript.sections, _uiState.value.positionMs)

    fun setChineseOnly(enabled: Boolean) {
        val lessonId = _uiState.value.lessonId ?: return
        if (!_uiState.value.chineseOnlyAvailable || _uiState.value.track == "practice") return
        playerConnection.switchTrack(if (enabled) "guided_zh" else "guided")
        if (enabled) setTranscriptVietnamese(false)
        viewModelScope.launch { settingsRepository.setChineseOnlyEnabled(lessonId, "guided", enabled) }
    }

    fun setTranscriptViewMode(mode: TranscriptViewMode) {
        _uiState.update { it.copy(transcript = it.transcript.copy(viewMode = mode)) }
        viewModelScope.launch { settingsRepository.setTranscriptView(_uiState.value.track, mode.name) }
    }

    fun setTranscriptVietnamese(enabled: Boolean) {
        _uiState.update { it.copy(transcript = it.transcript.copy(showVietnamese = enabled)) }
        viewModelScope.launch { settingsRepository.setVietnameseEnabled(enabled) }
    }

    fun lookupToken(itemId: String) {
        val word = _uiState.value.transcript.items?.words?.firstOrNull { it.id == itemId } ?: return
        val line = _uiState.value.transcript.lines.firstOrNull { it.text.contains(word.hanzi) }
        _uiState.update { it.copy(transcript = it.transcript.copy(lookupWord = word, lookupLineText = line?.text)) }
    }

    fun lookupCharacter(char: String) = _uiState.update { it.copy(transcript = it.transcript.copy(lookupCharacter = char)) }
    fun dismissCharacter() = _uiState.update { it.copy(transcript = it.transcript.copy(lookupCharacter = null)) }

    private fun ensureCharactersLoaded(lessonId: String) {
        if (_uiState.value.lessonId == lessonId && _uiState.value.characterIndex != null) return
        viewModelScope.launch(Dispatchers.IO) {
            val index = courseRepository.charactersFile(lessonId)?.let { CharacterIndex(CourseJson.characters(it.readText())) }
            // Offline or a bad pack must only hide "Xem nét", never crash the app or drop the character index.
            val strokePack = runCatching { courseRepository.strokePackFile()?.let { CourseJson.strokePack(it.readText()) } }.getOrNull()
            _uiState.update { if (it.lessonId == lessonId) it.copy(characterIndex = index, strokePack = strokePack) else it }
        }
    }

    fun dismissLookup() {
        _uiState.update { it.copy(transcript = it.transcript.copy(lookupWord = null, lookupLineText = null)) }
    }

    fun playLookupClip() = viewModelScope.launch {
        val lessonId = _uiState.value.transcript.lessonId ?: return@launch
        val clip = _uiState.value.transcript.lookupWord?.clip ?: return@launch
        val file = withContext(Dispatchers.IO) { courseRepository.clipFile(lessonId, clip.file) } ?: run {
            _uiState.update { it.copy(message = "Clip not downloaded") }
            return@launch
        }
        playerConnection.playClip(file.toURI().toString())
    }

    private fun ensureTranscriptLoaded(lessonId: String, track: String) {
        val current = _uiState.value.transcript
        if (current.lessonId == lessonId && current.track == track) return
        transcriptLoadJob?.cancel()
        loadedTranscript = null
        _uiState.update {
            it.copy(ngheTranscriptVisible = false, transcript = TranscriptUiState(lessonId = lessonId, track = track))
        }
        transcriptLoadJob = viewModelScope.launch {
            val loaded = withContext(backgroundDispatcher) { courseRepository.loadTranscript(lessonId, track) } ?: return@launch
            val savedView = settingsRepository.transcriptView(track)
                ?.let { runCatching { TranscriptViewMode.valueOf(it) }.getOrNull() }
                ?: TranscriptViewMode.PINYIN_ONLY
            val chineseOnly = settingsRepository.chineseOnlyEnabled(lessonId, "guided") && track != "practice"
            val vi = settingsRepository.vietnameseEnabled() && !chineseOnly
            val repeatEachEnabled = loaded.lessonFormat == "modes_v1" && track in setOf("noi", "phan_xa") &&
                settingsRepository.repeatEachEnabled(lessonId, track)
            if (track == "guided" || track == "guided_zh") playerConnection.switchTrack(if (chineseOnly) "guided_zh" else "guided")
            loadedTranscript = loaded
            _uiState.update { state ->
                applyModePresentation(state.copy(
                    lessonFormat = loaded.lessonFormat,
                    transcript = TranscriptUiState(
                        lessonId = lessonId,
                        track = track,
                        lines = loaded.lines,
                        cues = loaded.timeline.cues,
                        sections = loaded.timeline.sections,
                        items = loaded.items,
                        viewMode = savedView,
                        showVietnamese = vi,
                        loopedLineId = state.transcript.loopedLineId,
                    ),
                ), state.positionMs)
            }
            playerConnection.setRepeatEach(repeatEachEnabled)
        }
    }

    private fun applyModePresentation(state: PlayerUiState, positionMs: Long): PlayerUiState {
        val loaded = loadedTranscript ?: return state.copy(activeGap = null)
        val presentation = modePresentation(
            state.lessonFormat, state.track, loaded.timeline, loaded.lines, positionMs, state.ngheTranscriptVisible,
        )
        return state.copy(
            activeGap = presentation.activeGap,
            transcript = state.transcript.copy(lines = presentation.lines),
        )
    }

    private fun ensureLessonMeta(lessonId: String) {
        if (_uiState.value.lessonId == lessonId && _uiState.value.lessonTitle != null) return
        practiceLookupJob?.cancel()
        practiceLookupJob = viewModelScope.launch {
            val title = lessonTitle(lessons, lessonId)
            val catalogLesson = courseRepository.catalogLesson(lessonId)
            val tracks = catalogLesson?.tracks
            val chineseOnlyDownloaded = courseRepository.trackFile(lessonId, "guided_zh") != null
            _uiState.update { state ->
                if (state.lessonId != lessonId) state else applyModePresentation(
                    state.copy(
                        lessonTitle = title,
                        lessonFormat = catalogLesson?.format ?: "linear_v1",
                        practiceAvailable = tracks?.practice != null,
                        chineseOnlyAvailable = chineseOnlyDownloaded,
                    ),
                    state.positionMs,
                )
            }
        }
    }
}
