package com.hanziflow.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanziflow.data.CourseRepository
import com.hanziflow.shared.CourseJson
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Read-only: never touches the player, so audio keeps playing while notes are open. */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotesUiState())
    val uiState: StateFlow<NotesUiState> = _uiState.asStateFlow()

    fun load(lessonId: String) = viewModelScope.launch {
        val loaded = withContext(Dispatchers.IO) {
            val notes = courseRepository.notesFile(lessonId)?.let { runCatching { CourseJson.notes(it.readText()) }.getOrNull() }
                ?: return@withContext null
            val words = courseRepository.itemsFile(lessonId)
                ?.let { runCatching { CourseJson.items(it.readText()).words }.getOrNull() }.orEmpty()
            NotesUiState(
                lessonId = lessonId,
                title = courseRepository.catalogLesson(lessonId)?.title.orEmpty(),
                patterns = notes.patterns,
                questions = notes.questions,
                words = words,
            )
        }
        _uiState.value = loaded ?: NotesUiState()
    }
}
