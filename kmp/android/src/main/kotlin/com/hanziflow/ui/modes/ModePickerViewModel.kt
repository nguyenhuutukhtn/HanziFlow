package com.hanziflow.ui.modes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanziflow.data.CourseRepository
import com.hanziflow.shared.modes.ModeInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ModePickerViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ModePickerUiState())
    val uiState: StateFlow<ModePickerUiState> = _uiState.asStateFlow()

    fun load(lessonId: String) = viewModelScope.launch {
        val lesson = courseRepository.catalogLesson(lessonId) ?: return@launch
        _uiState.value = ModePickerUiState(
            lessonId = lesson.id,
            title = lesson.title,
            notesAvailable = courseRepository.notesFile(lesson.id) != null,
            modes = ModeInfo.all.mapNotNull { mode ->
                lesson.tracks.forName(mode.id)?.let { track ->
                    ModePickerItem(mode.id, mode.label, mode.whenToUse, track.durationMs)
                }
            },
        )
    }
}
