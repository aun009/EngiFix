package com.example.auth.presentation.features.contest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ContestNotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ContestNotificationReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val contestId = intent.getIntExtra("contest_id", -1)
        if (contestId == -1) {
            Log.e(TAG, "Invalid contest ID")
            return
        }

        val contestEvent = intent.getStringExtra("contest_event") ?: "Coding contest"
        val platformName = intent.getStringExtra("contest_platform") ?: "Contest"
        val contestHref = intent.getStringExtra("contest_href") ?: ""
        val contestStart = intent.getStringExtra("contest_start") ?: ""
        val contestStartMillis = intent.getLongExtra("contest_start_millis", -1L).takeIf { it > 0L }

        val contest = ContestItem(
            id = contestId,
            event = contestEvent,
            start = contestStart,
            end = "",
            duration = "",
            resource = "",
            resource_id = 0,
            host = "",
            href = contestHref
        )

        val shown = NotificationHelper(context).showContestReminder(
            contest = contest,
            platformName = platformName,
            contestStartMillis = contestStartMillis
        )

        Log.d(TAG, "Contest reminder ${if (shown) "shown" else "skipped"} for: $contestEvent")
    }
}
