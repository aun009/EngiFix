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

    init {
        if (sheetId.isBlank()) {
            _uiState.value = SheetDetailUiState.Error("Invalid sheet ID")
        } else {
            observeSheet()
        }
    }

    private fun observeSheet() {
        viewModelScope.launch {
            combine(
                repo.getSheetById(sheetId),
                repo.getQuestionsForSheet(sheetId),
                repo.getCompletedCount(sheetId),
                repo.getTotalCount(sheetId)
            ) { sheet, questions, completed, total ->
                if (sheet == null) {
                    SheetDetailUiState.Error("Sheet not found")
                } else {
                    // Group questions by topic, preserving insertion order within each topic
                    val grouped = questions.groupBy { it.topic }
                    SheetDetailUiState.Success(
                        SheetDetailData(
                            sheet = sheet,
                            groupedQuestions = grouped,
                            completedCount = completed,
                            totalCount = total
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
}
