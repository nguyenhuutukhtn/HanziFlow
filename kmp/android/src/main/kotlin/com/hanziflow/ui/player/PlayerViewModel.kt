package com.hanziflow.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanziflow.data.CourseRepository
import com.hanziflow.data.LessonEntity
import com.hanziflow.data.SettingsRepository
import com.hanziflow.playback.PlayerConnection
import com.hanziflow.shared.CourseJson
import com.hanziflow.shared.TimelineSection
import com.hanziflow.shared.transcript.SectionNavigation
import com.hanziflow.shared.transcript.TranscriptCues
import com.hanziflow.shared.transcript.TranscriptViewMode
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val settingsRepository: SettingsRepository,
    private val playerConnection: PlayerConnection,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val lessons = courseRepository.lessons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var transcriptLoadJob: Job? = null
    private var practiceLookupJob: Job? = null

    init {
        viewModelScope.launch {
            playerConnection.state.collect { playback ->
                _uiState.update {
                    it.copy(
                        connected = playback.connected,
                        playing = playback.playing,
                        lessonId = playback.lessonId,
                        track = playback.track,
                        positionMs = playback.positionMs,
                        durationMs = playback.durationMs,
                        speed = playback.speed,
                        sectionLoop = playback.sectionLoop,
                        chineseOnly = playback.chineseOnly,
                        transcript = it.transcript.copy(loopedLineId = playback.loopedLineId),
                    )
                }
                val id = playback.lessonId
                if (id != null) {
                    ensureTranscriptLoaded(id, playback.track)
                    ensureLessonMeta(id)
                }
            }
        }
    }

    fun play(lesson: LessonEntity, track: String = "guided") = viewModelScope.launch {
        val file = courseRepository.trackFile(lesson.id, track)
        if (file == null) {
            _uiState.update { it.copy(message = "Download this lesson first") }
            return@launch
        }
        playerConnection.play(lesson.id, track, file.toURI().toString(), lesson.title)
    }

    fun switchTrack(track: String) = viewModelScope.launch {
        val id = _uiState.value.lessonId ?: return@launch
        val lesson = lessons.value.firstOrNull { it.id == id } ?: return@launch
        play(lesson, track)
    }

    fun toggle() = playerConnection.toggle()
    fun seekBack() = playerConnection.seekBack()
    fun seekForward() = playerConnection.seekForward()
    fun seekTo(position: Long) = playerConnection.seekTo(position)
    fun speed(value: Float) = playerConnection.setSpeed(value)

    fun seekToLine(lineId: String) {
        val transcript = _uiState.value.transcript
        val target = TranscriptCues.seekTargetMs(transcript.cues, lineId, _uiState.value.positionMs) ?: return
        seekTo(target)
    }

    /** Section bottom sheet row tap: seek to that section's start and close the sheet (UI-side). */
    fun seekToSection(sectionId: String) {
        val target = SectionNavigation.sectionStart(_uiState.value.transcript.sections, sectionId) ?: return
        seekTo(target)
    }

    /**
     * Mini-player "Nghe lại câu": one-shot seek to the start of the line whose cue
     * contains the current position. No timeline (or a gap between cues) is a no-op,
     * never a seek to 0, so a paused/not-loaded player can't jump the lesson.
     */
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

    /** "Lặp phần này" for the section currently playing. */
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
        val track = _uiState.value.track
        playerConnection.setChineseOnly(enabled)
        if (enabled) setTranscriptVietnamese(false)
        viewModelScope.launch { settingsRepository.setChineseOnlyEnabled(lessonId, track, enabled) }
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
        _uiState.update { it.copy(transcript = it.transcript.copy(lookupWord = word)) }
    }

    fun dismissLookup() {
        _uiState.update { it.copy(transcript = it.transcript.copy(lookupWord = null)) }
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
        _uiState.update { it.copy(transcript = TranscriptUiState(lessonId = lessonId, track = track)) }
        transcriptLoadJob = viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                val referenceFile = courseRepository.referenceFile(lessonId) ?: return@withContext null
                val timelineFile = courseRepository.timelineFile(lessonId, track) ?: return@withContext null
                val reference = CourseJson.reference(referenceFile.readText())
                val lines = if (track == "practice") reference.tracks.practice.orEmpty() else reference.tracks.guided
                val timeline = CourseJson.timeline(timelineFile.readText())
                val items = courseRepository.itemsFile(lessonId)?.let { CourseJson.items(it.readText()) }
                Loaded(lines, timeline.cues, timeline.sections, items)
            } ?: return@launch
            val savedView = settingsRepository.transcriptView(track)
                ?.let { runCatching { TranscriptViewMode.valueOf(it) }.getOrNull() }
                ?: TranscriptViewMode.PINYIN_ONLY
            val chineseOnly = settingsRepository.chineseOnlyEnabled(lessonId, track)
            val vi = settingsRepository.vietnameseEnabled() && !chineseOnly
            playerConnection.setChineseOnly(chineseOnly)
            _uiState.update {
                it.copy(
                    transcript = TranscriptUiState(
                        lessonId = lessonId,
                        track = track,
                        lines = loaded.lines,
                        cues = loaded.cues,
                        sections = loaded.sections,
                        items = loaded.items,
                        viewMode = savedView,
                        showVietnamese = vi,
                        loopedLineId = it.transcript.loopedLineId,
                    ),
                )
            }
        }
    }

    private data class Loaded(
        val lines: List<com.hanziflow.shared.ReferenceLine>,
        val cues: List<com.hanziflow.shared.TimelineCue>,
        val sections: List<TimelineSection>,
        val items: com.hanziflow.shared.Items?,
    )

    private fun ensureLessonMeta(lessonId: String) {
        if (_uiState.value.lessonId == lessonId && _uiState.value.lessonTitle != null) return
        practiceLookupJob?.cancel()
        practiceLookupJob = viewModelScope.launch {
            // The playback state can arrive before Room's first lessons emission
            // (restored session on cold start), and a paused player emits no further
            // state changes. Wait for the lesson row instead of leaving the id as title.
            val title = lessons.value.firstOrNull { it.id == lessonId }?.title
                ?: withTimeoutOrNull(2_000) {
                    lessons.first { list -> list.any { it.id == lessonId } }
                        .first { it.id == lessonId }
                        .title
                }
            val practiceAvailable = courseRepository.catalogLesson(lessonId)?.tracks?.practice != null
            _uiState.update { it.copy(lessonTitle = title, practiceAvailable = practiceAvailable) }
        }
    }
}
