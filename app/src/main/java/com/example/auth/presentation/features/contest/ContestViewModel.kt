package com.example.auth.presentation.features.contest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

data class ContestUiModel(
    val raw: ContestItem,
    val isCurrentlyRunning: Boolean,
    val remainingTimeFormatted: String?,
    val formattedStartTime: String,
    val formattedEndTime: String,
    val formattedStartDateTime: String,
    val formattedEndDateTime: String,
    val formattedDuration: String,
    val platformName: String,
    val startMillis: Long,
    val endMillis: Long?
)

sealed class ContestUiState {
    object Loading : ContestUiState()
    data class Success(
        val todayContests: List<ContestUiModel>,
        val tomorrowContests: List<ContestUiModel>
    ) : ContestUiState()
    data class Error(val message: String) : ContestUiState()
}

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
            _uiState.value = ContestUiState.Loading
            try {
                // Network call
                val map = repository.getContestsSortedByPlatform()

                // Heavy date parsing offloaded to IO/Default thread to avoid blocking Main/UI
                val processedParams = withContext(Dispatchers.Default) {
                    processContests(map.values.flatten())
                }

                _uiState.value = ContestUiState.Success(
                    todayContests = processedParams.first,
                    tomorrowContests = processedParams.second
                )

                notificationHelper?.let { helper ->
                    map.forEach { (platform, contests) ->
                        helper.checkAndNotifyUpcomingContests(contests, platform)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = ContestUiState.Error("Failed to load contests: ${e.localizedMessage}")
            }
        }
    }

    private fun processContests(allContests: List<ContestItem>): Pair<List<ContestUiModel>, List<ContestUiModel>> {
        val todayContests = mutableListOf<ContestUiModel>()
        val tomorrowContests = mutableListOf<ContestUiModel>()

        val today = Calendar.getInstance()
        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

        for (contest in allContests) {
            val startCal = ContestDateTimeFormatter.parseToCalendar(contest.start) ?: continue
            val endCal = ContestDateTimeFormatter.parseToCalendar(contest.end)

            val startYear = startCal.get(Calendar.YEAR)
            val startMonth = startCal.get(Calendar.MONTH)
            val startDay = startCal.get(Calendar.DAY_OF_MONTH)

            val startsToday = today.get(Calendar.YEAR) == startYear &&
                    today.get(Calendar.MONTH) == startMonth &&
                    today.get(Calendar.DAY_OF_MONTH) == startDay

            val startsTomorrow = tomorrow.get(Calendar.YEAR) == startYear &&
                    tomorrow.get(Calendar.MONTH) == startMonth &&
                    tomorrow.get(Calendar.DAY_OF_MONTH) == startDay

            var isCurrentlyRunning = false
            var remainingTime: String? = null

            if (endCal != null) {
                val now = Calendar.getInstance().timeInMillis
                val startTime = startCal.timeInMillis
                val endTime = endCal.timeInMillis

                isCurrentlyRunning = startTime <= now && endTime > now

                if (isCurrentlyRunning) {
                    val diff = endTime - now
                    if (diff > 0) {
                        val hours = (diff / (1000 * 60 * 60)).toInt()
                        val minutes = ((diff % (1000 * 60 * 60)) / (1000 * 60)).toInt()
                        remainingTime = when {
                            hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                            hours > 0 -> "${hours}h"
                            minutes > 0 -> "${minutes}m"
                            else -> "Ending soon"
                        }
                    } else {
                        remainingTime = "Ending soon"
                    }
                }
            }

            if (startsToday || isCurrentlyRunning || startsTomorrow) {
                val uiModel = ContestUiModel(
                    raw = contest,
                    isCurrentlyRunning = isCurrentlyRunning,
                    remainingTimeFormatted = remainingTime,
                    formattedStartTime = ContestDateTimeFormatter.formatCardTime(startCal),
                    formattedEndTime = if (endCal != null) ContestDateTimeFormatter.formatCardTime(endCal) else "TBD",
                    formattedStartDateTime = ContestDateTimeFormatter.formatDetailDateTime(startCal),
                    formattedEndDateTime = if (endCal != null) ContestDateTimeFormatter.formatDetailDateTime(endCal) else "TBD",
                    formattedDuration = ContestDateTimeFormatter.formatDuration(contest.duration),
                    platformName = when (contest.resource) {
                        "codeforces.com" -> "Codeforces"
                        "codechef.com" -> "CodeChef"
                        "atcoder.jp" -> "AtCoder"
                        "leetcode.com" -> "LeetCode"
                        else -> contest.resource
                    },
                    startMillis = startCal.timeInMillis,
                    endMillis = endCal?.timeInMillis
                )

                if (startsToday || isCurrentlyRunning) {
                    todayContests.add(uiModel)
                } else if (startsTomorrow) {
                    tomorrowContests.add(uiModel)
                }
            }
        }

        todayContests.sortBy { it.startMillis }
        tomorrowContests.sortBy { it.startMillis }

        return Pair(todayContests, tomorrowContests)
    }
}
