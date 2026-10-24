package com.hanziflow.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ExplainUiState {
    data object NotConfigured : ExplainUiState
    data object Loading : ExplainUiState

    /** First explanation failed and nothing is saved; `retry()` tries again. */
    data class Failed(val error: String) : ExplainUiState
    data class Ready(val thread: ExplainThread, val sending: Boolean = false, val error: String? = null) : ExplainUiState
}

@HiltViewModel
class ExplainViewModel @Inject constructor(
    private val repository: ExplainRepository,
    private val config: AiConfig,
) : ViewModel() {
    private val _state = MutableStateFlow<ExplainUiState>(if (config.isConfigured) ExplainUiState.Loading else ExplainUiState.NotConfigured)
    val state: StateFlow<ExplainUiState> = _state.asStateFlow()

    private var target: ExplainTarget? = null
    private var job: Job? = null

    fun open(target: ExplainTarget) {
        this.target = target
        run(force = false)
    }

    fun reexplain() = run(force = true)

    fun send(question: String) {
        val t = target ?: return
        val ready = _state.value as? ExplainUiState.Ready ?: return
        if (question.isBlank() || ready.sending) return
        sendWith(ready) { repository.ask(t.lessonId, t.line.id, question.trim()) }
    }

    fun retry() {
        val t = target ?: return
        when (val s = _state.value) {
            is ExplainUiState.Failed -> run(force = false)
            is ExplainUiState.Ready -> if (!s.sending) sendWith(s) { repository.retry(t.lessonId, t.line.id) }
            else -> Unit
        }
    }

    private fun sendWith(ready: ExplainUiState.Ready, call: suspend () -> Result<ExplainThread>) {
        val t = target ?: return
        _state.value = ready.copy(sending = true, error = null)
        job = viewModelScope.launch {
            val result = call()
            val thread = repository.load(t.lessonId, t.line.id, t.line.text) ?: ready.thread
            _state.value = ExplainUiState.Ready(thread, sending = false, error = result.exceptionOrNull()?.message)
        }
    }

    private fun run(force: Boolean) {
        val t = target ?: return
        if (!config.isConfigured) {
            _state.value = ExplainUiState.NotConfigured
            return
        }
        job?.cancel()
        _state.value = ExplainUiState.Loading
        job = viewModelScope.launch {
            val result = repository.explain(t, force)
            _state.value = result.fold(
                onSuccess = { ExplainUiState.Ready(it) },
                onFailure = { ExplainUiState.Failed(it.message ?: "Error") },
            )
        }
    }
}
