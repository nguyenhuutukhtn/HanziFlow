package com.hanziflow.ui.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hanziflow.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppShellUiState(
    /** Dark is the first-run default; the toggle lands with the Settings tab in G2b. */
    val darkTheme: Boolean = SettingsRepository.DEFAULT_DARK_THEME,
)

@HiltViewModel
class AppShellViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppShellUiState())
    val uiState: StateFlow<AppShellUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(darkTheme = settingsRepository.darkTheme()) }
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        _uiState.update { it.copy(darkTheme = enabled) }
        viewModelScope.launch { settingsRepository.setDarkTheme(enabled) }
    }
}
