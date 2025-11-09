package com.example.auth.presentation.features.contest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.text.SimpleDateFormat
import java.util.*

class NotificationHelper(private val context: Context) {
    
    companion object {
        const val CHANNEL_ID = "contest_notifications"
        const val CHANNEL_NAME = "Contest Reminders"
        const val CHANNEL_DESCRIPTION = "Notifications for upcoming coding contests"
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showContestReminder(contest: ContestItem, platformName: String) {
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(contest.href))
        val pendingIntent = PendingIntent.getActivity(
            context,
            contest.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🚀 Contest Starting Soon!")
            .setContentText("${contest.event} on $platformName")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${contest.event} on $platformName\nStarts in 15 minutes!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(contest.id, notification)
        }
    }
    
    fun checkAndNotifyUpcomingContests(contests: List<ContestItem>, platformName: String) {
        val currentTime = Calendar.getInstance()
        val fifteenMinutesFromNow = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 15)
        }
        
        contests.forEach { contest ->
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val contestStart = inputFormat.parse(contest.start)
                
                contestStart?.let { startTime ->
                    val contestCalendar = Calendar.getInstance()
                    contestCalendar.time = startTime
                    
                    // Check if contest starts within the next 15 minutes
                    if (contestCalendar.after(currentTime) && contestCalendar.before(fifteenMinutesFromNow)) {
                        showContestReminder(contest, platformName)
                    }
                }
            } catch (e: Exception) {
                println("Error parsing contest time for notification: ${e.message}")
            }
        }
    }
}
