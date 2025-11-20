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
        Log.d(TAG, "📢 Notification receiver triggered")
        
        val contestId = intent.getIntExtra("contest_id", -1)
        val contestEvent = intent.getStringExtra("contest_event") ?: "Contest"
        val platformName = intent.getStringExtra("contest_platform") ?: "Platform"
        val contestHref = intent.getStringExtra("contest_href") ?: ""
        
        if (contestId == -1) {
            Log.e(TAG, "Invalid contest ID")
            return
        }
        
        // Create a contest item for the notification
        val contest = ContestItem(
            id = contestId,
            event = contestEvent,
            start = "",
            end = "",
            duration = "",
            resource = "",
            resource_id = 0,
            host = "",
            href = contestHref
        )
        
        // Show the notification
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showContestReminder(contest, platformName)
        
        Log.d(TAG, "✅ Notification shown for: $contestEvent on $platformName")
    }
}

