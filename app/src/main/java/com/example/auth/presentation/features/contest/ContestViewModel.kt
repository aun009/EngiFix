package com.example.auth.presentation.features.contest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class ContestUiModel(
    val raw: ContestItem,
    val isCurrentlyRunning: Boolean,
    val remainingTimeFormatted: String?,
    val formattedStartTime: String,
    val formattedEndTime: String,
    val formattedDuration: String,
    val platformName: String
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
            val startCal = parseContestDate(contest.start) ?: continue
            val endCal = parseContestDate(contest.end)

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
                    formattedStartTime = formatDateTime(startCal),
                    formattedEndTime = if (endCal != null) formatDateTime(endCal) else contest.end,
                    formattedDuration = contest.duration.replace(" hours", "h")
                                                        .replace(" hour", "h")
                                                        .replace(" minutes", "m")
                                                        .replace(" minute", "m"),
                    platformName = when (contest.resource) {
                        "codeforces.com" -> "Codeforces"
                        "codechef.com" -> "CodeChef"
                        "atcoder.jp" -> "AtCoder"
                        "leetcode.com" -> "LeetCode"
                        else -> contest.resource
                    }
                )

                if (startsToday || isCurrentlyRunning) {
                    todayContests.add(uiModel)
                } else if (startsTomorrow) {
                    tomorrowContests.add(uiModel)
                }
            }
        }

        todayContests.sortBy { it.raw.start }
        tomorrowContests.sortBy { it.raw.start }

        return Pair(todayContests, tomorrowContests)
    }

    private fun parseContestDate(dateString: String): Calendar? {
        try {
            val timestamp = dateString.toLongOrNull()
            if (timestamp != null) {
                val date = if (timestamp > 1000000000000L) Date(timestamp) else Date(timestamp * 1000)
                return Calendar.getInstance().apply { time = date }
            }

            val formats = listOf(
                "dd.MM EEE HH:mm", "d.MM EEE HH:mm", "dd.M EEE HH:mm", "d.M EEE HH:mm",
                "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm:ssXXX"
            )

            var parsedDate: Date? = null
            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.ENGLISH)
                    if (format.contains("dd.MM") || format.contains("d.M")) {
                        val parsed = sdf.parse(dateString)
                        if (parsed != null) {
                            val tempCal = Calendar.getInstance().apply { time = parsed }
                            val now = Calendar.getInstance()
                            val currentYear = now.get(Calendar.YEAR)
                            val parsedMonth = tempCal.get(Calendar.MONTH)
                            val year = if (parsedMonth < now.get(Calendar.MONTH)) currentYear + 1 else currentYear
                            tempCal.set(Calendar.YEAR, year)
                            parsedDate = tempCal.time
                            break
                        }
                    } else {
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        parsedDate = sdf.parse(dateString)
                        if (parsedDate != null) break
                    }
                } catch (e: Exception) {}
            }

            if (parsedDate == null) return null

            return Calendar.getInstance().apply { timeInMillis = parsedDate.time }
        } catch (e: Exception) {
            return null
        }
    }

    private fun formatDateTime(cal: Calendar): String {
        val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Mon"; Calendar.TUESDAY -> "Tue"; Calendar.WEDNESDAY -> "Wed"
            Calendar.THURSDAY -> "Thu"; Calendar.FRIDAY -> "Fri"; Calendar.SATURDAY -> "Sat"
            Calendar.SUNDAY -> "Sun"; else -> ""
        }
        return String.format("%02d.%02d %s %02d:%02d",
            cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, dayOfWeek,
            cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }
}
