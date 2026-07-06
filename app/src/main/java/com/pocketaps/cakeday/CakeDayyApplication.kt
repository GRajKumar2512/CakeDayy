package com.pocketaps.cakeday

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.pocketaps.cakeday.core.notifications.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

@HiltAndroidApp
class CakeDayyApplication : Application(), Configuration.Provider {

    @Inject lateinit var hiltWorkerFactory: HiltWorkerFactory

    @Inject lateinit var reminderScheduler: ReminderScheduler

    @Inject lateinit var widgetUpdateCoordinator: WidgetUpdateCoordinator

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(hiltWorkerFactory).build()

    override fun onCreate() {
        super.onCreate()
        reminderScheduler.scheduleDailyReminders()
        widgetUpdateCoordinator.start(applicationScope)
    }
}
