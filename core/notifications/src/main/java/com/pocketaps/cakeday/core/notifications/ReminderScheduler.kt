package com.pocketaps.cakeday.core.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.pocketaps.cakeday.core.notifications.worker.BirthdayReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    // WorkManager persists its schedule across reboots (ARCHITECTURE.md §7), so no
    // BOOT_COMPLETED receiver is needed to re-arm this work after a device restart.
    fun scheduleDailyReminders() {
        val request = PeriodicWorkRequestBuilder<BirthdayReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            BirthdayReminderWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
