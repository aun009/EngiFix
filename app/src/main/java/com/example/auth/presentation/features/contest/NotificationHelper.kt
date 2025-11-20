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
import java.util.concurrent.TimeUnit

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
                .bigText("${contest.event} on $platformName\nStarts in 20 minutes!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(contest.id, notification)
            android.util.Log.d("NotificationHelper", "✅ Notification sent for: ${contest.event}")
        } else {
            android.util.Log.w("NotificationHelper", "⚠️ Notifications are disabled")
        }
    }
    
    fun checkAndNotifyUpcomingContests(contests: List<ContestItem>, platformName: String) {
        val currentTime = Calendar.getInstance()
        val twentyMinutesFromNow = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 20)
        }
        
        // Get shared preferences to track notified contests
        val prefs = context.getSharedPreferences("contest_notifications", Context.MODE_PRIVATE)
        
        contests.forEach { contest ->
            try {
                // Try multiple date formats
                val formats = listOf(
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()),
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault())
                )
                
                var contestStart: Date? = null
                for (format in formats) {
                    try {
                        contestStart = format.parse(contest.start)
                        if (contestStart != null) break
                    } catch (e: Exception) {
                        // Try next format
                    }
                }
                
                contestStart?.let { startTime ->
                    val contestCalendar = Calendar.getInstance()
                    contestCalendar.time = startTime
                    
                    // Calculate time 20 minutes before contest
                    val notificationTime = Calendar.getInstance()
                    notificationTime.time = startTime
                    notificationTime.add(Calendar.MINUTE, -20)
                    
                    val currentTimeInMillis = currentTime.timeInMillis
                    val notificationTimeInMillis = notificationTime.timeInMillis
                    val twentyMinutesFromNowInMillis = twentyMinutesFromNow.timeInMillis
                    
                    // Check if we should notify now (within the 20-minute window)
                    val shouldNotifyNow = notificationTimeInMillis <= currentTimeInMillis && 
                                          currentTimeInMillis <= notificationTimeInMillis + TimeUnit.MINUTES.toMillis(5)
                    
                    // Check if contest starts within the next 20 minutes
                    val contestStartsSoon = contestCalendar.after(currentTime) && 
                                           contestCalendar.before(twentyMinutesFromNow)
                    
                    // Check if we've already notified for this contest
                    val notificationKey = "notified_${contest.id}_${contest.start}"
                    val alreadyNotified = prefs.getBoolean(notificationKey, false)
                    
                    if ((shouldNotifyNow || contestStartsSoon) && !alreadyNotified) {
                        showContestReminder(contest, platformName)
                        // Mark as notified
                        prefs.edit().putBoolean(notificationKey, true).apply()
                        android.util.Log.d("NotificationHelper", "📢 Scheduled notification for: ${contest.event} at ${contest.start}")
                    }
                } ?: run {
                    android.util.Log.w("NotificationHelper", "⚠️ Could not parse date: ${contest.start}")
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationHelper", "❌ Error parsing contest time: ${e.message}", e)
            }
        }
    }
}
