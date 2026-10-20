package com.hanziflow.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanziflow.data.CourseRepository
import com.hanziflow.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val courseRepository: CourseRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(baseUrl = settingsRepository.baseUrl()) }
        }
        viewModelScope.launch {
            courseRepository.lessons.collect { list -> _uiState.update { it.copy(lessons = list) } }
        }
    }

    fun editBaseUrl(value: String) {
        _uiState.update { it.copy(baseUrl = value) }
    }

    fun sync() = viewModelScope.launch {
        _uiState.update { it.copy(message = "Syncing catalog…") }
        runCatching {
            settingsRepository.setBaseUrl(_uiState.value.baseUrl)
            courseRepository.syncCatalog()
        }.onSuccess {
            _uiState.update { it.copy(message = "Catalog updated") }
        }.onFailure { error ->
            _uiState.update { it.copy(message = error.message ?: "Catalog sync failed") }
        }
    }

    fun download(id: String) = courseRepository.download(id)
    fun cancel(id: String) = courseRepository.cancel(id)
    fun delete(id: String) = viewModelScope.launch { courseRepository.delete(id) }
}
