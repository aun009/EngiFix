package com.example.auth.presentation.features.sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.data.local.DsaRepository
import com.example.auth.data.local.entity.ExploreSheetEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SheetListUiState {
    data object Loading : SheetListUiState
    data class Success(val sheets: List<ExploreSheetEntity>) : SheetListUiState
    data object Empty : SheetListUiState
    data class Error(val message: String) : SheetListUiState
}

@HiltViewModel
class SheetListViewModel @Inject constructor(
    private val repo: DsaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SheetListUiState>(SheetListUiState.Loading)
    val uiState: StateFlow<SheetListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // 1. Sync from GitHub (or fall back to assets) — shows shimmer while this runs
            repo.syncIfNeeded()

            // 2. Observe Room DB — updates automatically if sync added new data
            repo.getAllSheets()
                .catch { e -> _uiState.value = SheetListUiState.Error(e.message ?: "Failed to load sheets") }
                .collect { sheets ->
                    _uiState.value = if (sheets.isEmpty()) SheetListUiState.Empty
                    else SheetListUiState.Success(sheets)
                }
        }
    }
}
