package com.pocketaps.cakeday.core.notifications.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pocketaps.cakeday.core.domain.usecase.GetDueRemindersUseCase
import com.pocketaps.cakeday.core.notifications.BirthdayNotifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class BirthdayReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val getDueReminders: GetDueRemindersUseCase,
    private val birthdayNotifier: BirthdayNotifier,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        getDueReminders().forEach { birthdayNotifier.notify(it) }
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "birthday_reminder_daily"
    }
}
