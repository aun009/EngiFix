package com.example.auth.presentation.features.sheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.data.local.DsaRepository
import com.example.auth.data.local.entity.DsaQuestionEntity
import com.example.auth.data.local.entity.ExploreSheetEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SheetDetailData(
    val sheet: ExploreSheetEntity,
    val groupedQuestions: Map<String, List<DsaQuestionEntity>>, // topic → questions
    val completedCount: Int,
    val totalCount: Int,
    val isRefreshing: Boolean,
    val syncError: String?
)

private data class SheetSnapshot(
    val sheet: ExploreSheetEntity?,
    val questions: List<DsaQuestionEntity>,
    val completedCount: Int,
    val totalCount: Int
)

sealed interface SheetDetailUiState {
    data object Loading : SheetDetailUiState
    data class Success(val data: SheetDetailData) : SheetDetailUiState
    data class Error(val message: String) : SheetDetailUiState
}

@HiltViewModel
class SheetDetailViewModel @Inject constructor(
    private val repo: DsaRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sheetId: String = savedStateHandle.get<String>("sheetId") ?: ""

    private val _uiState = MutableStateFlow<SheetDetailUiState>(SheetDetailUiState.Loading)
    val uiState: StateFlow<SheetDetailUiState> = _uiState.asStateFlow()

    private val isRefreshing = MutableStateFlow(false)
    private val syncError = MutableStateFlow<String?>(null)
    private val initialSyncFinished = MutableStateFlow(false)

    init {
        if (sheetId.isBlank()) {
            _uiState.value = SheetDetailUiState.Error("Invalid sheet ID")
        } else {
            observeSheet()
            refreshSheet()
        }
    }

    private fun observeSheet() {
        viewModelScope.launch {
            val sheetSnapshot = combine(
                repo.getSheetById(sheetId),
                repo.getQuestionsForSheet(sheetId),
                repo.getCompletedCount(sheetId),
                repo.getTotalCount(sheetId),
            ) { sheet, questions, completed, total ->
                SheetSnapshot(sheet, questions, completed, total)
            }

            combine(
                sheetSnapshot,
                isRefreshing,
                syncError,
                initialSyncFinished
            ) { snapshot, refreshing, error, syncFinished ->
                if (snapshot.sheet == null) {
                    if (syncFinished) SheetDetailUiState.Error("Sheet not found") else SheetDetailUiState.Loading
                } else {
                    // Group questions by topic, preserving insertion order within each topic
                    val grouped = snapshot.questions.groupBy { it.topic.ifBlank { "General" } }
                    SheetDetailUiState.Success(
                        SheetDetailData(
                            sheet = snapshot.sheet,
                            groupedQuestions = grouped,
                            completedCount = snapshot.completedCount,
                            totalCount = snapshot.totalCount,
                            isRefreshing = refreshing,
                            syncError = error
                        )
                    )
                }
            }
            .catch { e ->
                emit(SheetDetailUiState.Error(e.message ?: "Failed to load sheet details"))
            }
            .collect { _uiState.value = it }
        }
    }

    fun toggleCompletion(questionId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            repo.toggleCompletion(questionId, isCompleted)
        }
    }

    fun refreshSheet() {
        if (isRefreshing.value) return
        viewModelScope.launch {
            isRefreshing.value = true
            syncError.value = null
            val result = runCatching { repo.syncIfNeeded() }
            result.exceptionOrNull()?.let { e ->
                syncError.value = e.message ?: "Could not refresh sheet"
            }
            initialSyncFinished.value = true
            isRefreshing.value = false
        }
    }
}
