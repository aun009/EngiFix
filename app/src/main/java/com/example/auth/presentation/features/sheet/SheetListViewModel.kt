package com.example.auth.presentation.features.sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.data.local.DsaRepository
import com.example.auth.data.local.SheetProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SheetListUiState {
    data object Loading : SheetListUiState
    data class Success(
        val sheets: List<SheetProgress>,
        val isRefreshing: Boolean,
        val syncError: String?
    ) : SheetListUiState
    data class Empty(val isRefreshing: Boolean, val syncError: String?) : SheetListUiState
    data class Error(val message: String) : SheetListUiState
}

@HiltViewModel
class SheetListViewModel @Inject constructor(
    private val repo: DsaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SheetListUiState>(SheetListUiState.Loading)
    val uiState: StateFlow<SheetListUiState> = _uiState.asStateFlow()

    private val isRefreshing = MutableStateFlow(false)
    private val syncError = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            combine(
                repo.getSheetsWithProgress(),
                isRefreshing,
                syncError
            ) { sheets, refreshing, error ->
                if (sheets.isEmpty()) {
                    SheetListUiState.Empty(refreshing, error)
                } else {
                    SheetListUiState.Success(sheets, refreshing, error)
                }
            }
                .onStart { emit(SheetListUiState.Loading) }
                .catch { e -> _uiState.value = SheetListUiState.Error(e.message ?: "Failed to load sheets") }
                .collect { _uiState.value = it }
        }
        refreshSheets()
    }

    fun refreshSheets() {
        if (isRefreshing.value) return
        viewModelScope.launch {
            isRefreshing.value = true
            syncError.value = null
            val result = runCatching { repo.syncIfNeeded() }
            result.exceptionOrNull()?.let { e ->
                syncError.value = e.message ?: "Could not refresh sheets"
            }
            isRefreshing.value = false
        }
    }
}
