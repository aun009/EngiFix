package com.example.auth.presentation.features.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.data.jobs.JobRepository
import com.example.auth.data.local.entity.JobEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Filter state ──────────────────────────────────────────────────────────────

data class JobFilter(
    val type: String  = "All",    // All | Internship | Full-time
    val batch: String = "All"     // All | Freshers | 2025 | 2026 | 2027 | 2028 | 2029
)

val TYPE_OPTIONS  = listOf("All", "Internship", "Full-time")
val BATCH_OPTIONS = listOf("All", "Freshers", "2029", "2028", "2027", "2026", "2025", "2024")

// ── UI state ──────────────────────────────────────────────────────────────────

sealed interface JobsUiState {
    data object Loading : JobsUiState
    data object Scraping : JobsUiState                                 // first-time fetch
    data class Success(
        val jobs: List<JobEntity>,
        val totalCount: Int,
        val isRefreshing: Boolean = false
    ) : JobsUiState
    data object Empty : JobsUiState
    data class Error(val message: String) : JobsUiState
}

@HiltViewModel
class JobsViewModel @Inject constructor(
    private val repo: JobRepository
) : ViewModel() {

    private val _filter      = MutableStateFlow(JobFilter())
    val filter: StateFlow<JobFilter> = _filter.asStateFlow()

    private val _uiState     = MutableStateFlow<JobsUiState>(JobsUiState.Loading)
    val uiState: StateFlow<JobsUiState> = _uiState.asStateFlow()

    init { loadJobs() }

    // ── Public API ────────────────────────────────────────────────────────────

    fun setTypeFilter(type: String)  { _filter.update { it.copy(type  = type)  } }
    fun setBatchFilter(batch: String){ _filter.update { it.copy(batch = batch) } }

    fun refresh() {
        viewModelScope.launch {
            val cur = _uiState.value
            if (cur is JobsUiState.Success)
                _uiState.value = cur.copy(isRefreshing = true)
            try {
                repo.forceRefresh()
            } catch (e: Exception) {
                _uiState.value = JobsUiState.Error("No internet. Showing cached data.")
            }
        }
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun loadJobs() {
        viewModelScope.launch {
            // Show dedicated "fetching" state on first launch
            _uiState.value = JobsUiState.Scraping
            try { repo.refreshIfStale() } catch (_: Exception) {}

            // Combine raw list + filter → filtered UI state
            repo.getAllJobs()
                .combine(_filter) { jobs, f -> applyFilter(jobs, f) }
                .catch { e -> _uiState.value = JobsUiState.Error(e.message ?: "Error") }
                .collect { (total, filtered) ->
                    _uiState.value = when {
                        filtered.isEmpty() && total == 0 -> JobsUiState.Empty
                        else -> JobsUiState.Success(
                            jobs        = filtered,
                            totalCount  = total,
                            isRefreshing = (_uiState.value as? JobsUiState.Success)?.isRefreshing ?: false
                        )
                    }
                }
        }
    }

    private fun applyFilter(jobs: List<JobEntity>, f: JobFilter): Pair<Int, List<JobEntity>> {
        val filtered = jobs.filter { job ->
            val typeOk = f.type == "All" || job.category.contains(f.type, ignoreCase = true)
            val batchOk = f.batch == "All" || job.batchYears.contains(f.batch, ignoreCase = true)
            typeOk && batchOk
        }
        return jobs.size to filtered
    }
}
