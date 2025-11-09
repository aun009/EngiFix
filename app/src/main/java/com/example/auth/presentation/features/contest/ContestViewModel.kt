package com.example.auth.presentation.features.contest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ContestViewModel(
    private val repository: ContestRepository,
    private val notificationHelper: NotificationHelper? = null
): ViewModel() {
    private val _uiState = MutableStateFlow<ContestUiState>(ContestUiState.Loading)
    val uiState: StateFlow<ContestUiState> = _uiState

    init {
        fetchContests()
    }

    fun fetchContests() {
        viewModelScope.launch {
            println("🔄 ViewModel: Starting to fetch contests...")
            _uiState.value = ContestUiState.Loading
            try {
                val map = repository.getContestsSortedByPlatform()
                println("✅ ViewModel: Successfully got ${map.values.sumOf { it.size }} contests from repository")
                _uiState.value = ContestUiState.Success(map)

                // Check for upcoming contests and show notifications
                notificationHelper?.let { helper ->
                    map.forEach { (platform, contests) ->
                        helper.checkAndNotifyUpcomingContests(contests, platform)
                    }
                }
            } catch (e: Exception) {
                println("❌ ViewModel ERROR: ${e.message}")
                e.printStackTrace()
                _uiState.value = ContestUiState.Error("Failed to load contests: ${e.localizedMessage}")
            }
        }
    }
}


sealed class ContestUiState {
    object Loading : ContestUiState()
    data class Success(val contestsByPlatform: Map<String, List<ContestItem>>) : ContestUiState()
    data class Error(val message: String) : ContestUiState()
}
