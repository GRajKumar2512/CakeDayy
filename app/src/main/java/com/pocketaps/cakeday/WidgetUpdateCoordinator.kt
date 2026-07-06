package com.pocketaps.cakeday

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.pocketaps.cakeday.core.domain.usecase.GetUpcomingBirthdaysUseCase
import com.pocketaps.cakeday.core.notifications.worker.BirthdayReminderWorker
import com.pocketaps.cakeday.widget.CakeDayyWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetUpdateCoordinator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getUpcomingBirthdays: GetUpcomingBirthdaysUseCase,
) {
    fun start(scope: CoroutineScope) {
        refreshTriggers().onEach { CakeDayyWidget().updateAll(context) }.launchIn(scope)
    }

    // isDailyJobRunning is a parameter (rather than reading WorkManager inline) so tests can drive
    // the RUNNING/not-RUNNING sequence directly instead of depending on real WorkManager timing.
    internal fun refreshTriggers(
        isDailyJobRunning: Flow<Boolean> = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(BirthdayReminderWorker.UNIQUE_WORK_NAME)
            .map { infos -> infos.any { it.state == WorkInfo.State.RUNNING } },
    ): Flow<Unit> {
        // Room's Flow already re-emits on every write to the person table (add/edit/delete/
        // contacts-import/backup-import), regardless of write path, since Room's invalidation
        // tracker is table-based rather than tied to a specific DAO call site.
        val dataChanged = getUpcomingBirthdays().map { }

        // A PeriodicWorkRequest's WorkInfo never reaches SUCCEEDED — it cycles back to ENQUEUED
        // for the next period by design — so a completed run is detected via the RUNNING ->
        // not-RUNNING falling edge instead. This catches "the day rolled over, nothing in Room
        // changed", which the reactive Flow above would otherwise miss.
        val dailyJobFinished = isDailyJobRunning
            .distinctUntilChanged()
            .drop(1)
            .filter { isRunning -> !isRunning }
            .map { }

        return merge(dataChanged, dailyJobFinished)
    }
}
