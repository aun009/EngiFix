package com.example.auth.presentation.features.contest

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class ContestNotificationService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var alarmManager: AlarmManager
    private lateinit var contestRepository: ContestRepository
    private var periodicCheckStarted = false

    companion object {
        private const val TAG = "ContestNotificationService"
        private val CHECK_INTERVAL = TimeUnit.MINUTES.toMillis(10)
    }

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ContestServiceEntryPoint::class.java
        )
        contestRepository = entryPoint.getContestRepository()

        Log.d(TAG, "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!periodicCheckStarted) {
            periodicCheckStarted = true
            startPeriodicCheck()
        }
        return START_STICKY
    }

    private fun startPeriodicCheck() {
        checkUpcomingContests()

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
                val contestsByPlatform = contestRepository.getContestsSortedByPlatform()

                contestsByPlatform.forEach { (platformName, contests) ->
                    scheduleNotificationsForContests(contests, platformName)
                    notificationHelper.checkAndNotifyUpcomingContests(contests, platformName)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking contests: ${e.message}", e)
            }
        }
    }

    private fun scheduleNotificationsForContests(contests: List<ContestItem>, platformName: String) {
        val currentTime = System.currentTimeMillis()
        val prefs = getSharedPreferences("contest_notifications", Context.MODE_PRIVATE)

        contests.forEach { contest ->
            val contestStartMillis = ContestReminderPolicy.parseStartMillis(contest.start)
            if (contestStartMillis == null) {
                Log.w(TAG, "Could not parse contest start for ${contest.event}: ${contest.start}")
                return@forEach
            }

            val notificationTimeMillis = ContestReminderPolicy.reminderTimeMillis(contestStartMillis)

            when {
                notificationTimeMillis > currentTime -> {
                    val notificationKey = ContestReminderPolicy.scheduledKey(contest, contestStartMillis)
                    if (!prefs.getBoolean(notificationKey, false)) {
                        scheduleNotification(contest, platformName, contestStartMillis, notificationTimeMillis)
                        prefs.edit().putBoolean(notificationKey, true).apply()
                        Log.d(TAG, "Scheduled 25-minute reminder for ${contest.event} at ${Date(notificationTimeMillis)}")
                    }
                }
                ContestReminderPolicy.shouldNotifyNow(contestStartMillis, currentTime) -> {
                    notificationHelper.showContestReminder(contest, platformName, contestStartMillis)
                }
            }
        }
    }

    private fun scheduleNotification(
        contest: ContestItem,
        platformName: String,
        contestStartMillis: Long,
        notificationTimeMillis: Long
    ) {
        val intent = Intent(this, ContestNotificationReceiver::class.java).apply {
            putExtra("contest_id", contest.id)
            putExtra("contest_event", contest.event)
            putExtra("contest_platform", platformName)
            putExtra("contest_href", contest.href)
            putExtra("contest_start", contest.start)
            putExtra("contest_start_millis", contestStartMillis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            listOf(contest.id, contestStartMillis, ContestReminderPolicy.REMINDER_MINUTES).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTimeMillis,
                pendingIntent
            )
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, notificationTimeMillis, pendingIntent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ContestServiceEntryPoint {
    fun getContestRepository(): ContestRepository
}
