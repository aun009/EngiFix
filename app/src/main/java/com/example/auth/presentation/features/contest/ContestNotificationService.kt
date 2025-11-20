package com.example.auth.presentation.features.contest

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class ContestNotificationService : Service() {
    
    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var alarmManager: AlarmManager
    private lateinit var contestRepository: ContestRepository
    
    companion object {
        private const val TAG = "ContestNotificationService"
        private val CHECK_INTERVAL = TimeUnit.MINUTES.toMillis(10) // Check every 10 minutes
        private const val NOTIFICATION_TIME_BEFORE_CONTEST_MINUTES = 20L
    }
    
    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        // Get repository using EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ContestServiceEntryPoint::class.java
        )
        contestRepository = entryPoint.getContestRepository()
        
        Log.d(TAG, "Service created")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        startPeriodicCheck()
        return START_STICKY
    }
    
    private fun startPeriodicCheck() {
        // Immediately check for contests
        checkUpcomingContests()
        
        // Then check periodically
        val runnable = object : Runnable {
            override fun run() {
                checkUpcomingContests()
                handler.postDelayed(this, CHECK_INTERVAL)
            }
        }
        handler.postDelayed(runnable, CHECK_INTERVAL)
    }
    
    private fun checkUpcomingContests() {
        serviceScope.launch {
            try {
                Log.d(TAG, "🔔 Checking for upcoming contests...")
                
                // Fetch contests from repository
                val contestsByPlatform = contestRepository.getContestsSortedByPlatform()
                Log.d(TAG, "✅ Fetched ${contestsByPlatform.values.sumOf { it.size }} contests")
                
                // Schedule notifications for all contests
                contestsByPlatform.forEach { (platformName, contests) ->
                    scheduleNotificationsForContests(contests, platformName)
                    // Also check for immediate notifications (contests starting within 20 minutes)
                    notificationHelper.checkAndNotifyUpcomingContests(contests, platformName)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error checking contests: ${e.message}", e)
            }
        }
    }
    
    private fun scheduleNotificationsForContests(contests: List<ContestItem>, platformName: String) {
        val currentTime = System.currentTimeMillis()
        val prefs = getSharedPreferences("contest_notifications", Context.MODE_PRIVATE)
        
        contests.forEach { contest ->
            try {
                // Parse contest start time
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
                    val contestStartMillis = startTime.time
                    val notificationTimeMillis = contestStartMillis - TimeUnit.MINUTES.toMillis(NOTIFICATION_TIME_BEFORE_CONTEST_MINUTES)
                    
                    // Only schedule if notification time is in the future
                    if (notificationTimeMillis > currentTime) {
                        val notificationKey = "scheduled_${contest.id}_${contest.start}"
                        val alreadyScheduled = prefs.getBoolean(notificationKey, false)
                        
                        if (!alreadyScheduled) {
                            scheduleNotification(contest, platformName, notificationTimeMillis)
                            prefs.edit().putBoolean(notificationKey, true).apply()
                            Log.d(TAG, "📅 Scheduled notification for ${contest.event} at ${Date(notificationTimeMillis)}")
                        }
                    } else if (notificationTimeMillis <= currentTime && contestStartMillis > currentTime) {
                        // Contest is starting soon, notify immediately
                        val notificationKey = "notified_${contest.id}_${contest.start}"
                        val alreadyNotified = prefs.getBoolean(notificationKey, false)
                        
                        if (!alreadyNotified) {
                            notificationHelper.showContestReminder(contest, platformName)
                            prefs.edit().putBoolean(notificationKey, true).apply()
                            Log.d(TAG, "🔔 Immediate notification for ${contest.event}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error scheduling notification for contest ${contest.event}: ${e.message}", e)
            }
        }
    }
    
    private fun scheduleNotification(contest: ContestItem, platformName: String, notificationTimeMillis: Long) {
        val intent = Intent(this, ContestNotificationReceiver::class.java).apply {
            putExtra("contest_id", contest.id)
            putExtra("contest_event", contest.event)
            putExtra("contest_platform", platformName)
            putExtra("contest_href", contest.href)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            contest.id, // Use contest ID as request code for uniqueness
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTimeMillis,
                pendingIntent
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTimeMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTimeMillis, pendingIntent)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        Log.d(TAG, "Service destroyed")
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ContestServiceEntryPoint {
    fun getContestRepository(): ContestRepository
}
