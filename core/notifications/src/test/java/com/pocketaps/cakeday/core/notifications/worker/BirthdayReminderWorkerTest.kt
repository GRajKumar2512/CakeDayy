package com.pocketaps.cakeday.core.notifications.worker

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import com.pocketaps.cakeday.core.domain.usecase.GetDueRemindersUseCase
import com.pocketaps.cakeday.core.domain.usecase.GetUpcomingBirthdaysUseCase
import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.model.ReminderLead
import com.pocketaps.cakeday.core.notifications.BirthdayNotifier
import com.pocketaps.cakeday.core.testing.fake.FakePersonRepository
import com.pocketaps.cakeday.core.testing.fake.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BirthdayReminderWorkerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    private fun buildWorker(
        fakePersonRepo: FakePersonRepository,
        fakeSettingsRepo: FakeSettingsRepository,
    ): BirthdayReminderWorker {
        val getDueReminders = GetDueRemindersUseCase(GetUpcomingBirthdaysUseCase(fakePersonRepo), fakeSettingsRepo)
        val notifier = BirthdayNotifier(context)
        return TestListenableWorkerBuilder<BirthdayReminderWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters,
                ) = BirthdayReminderWorker(appContext, workerParameters, getDueReminders, notifier)
            })
            .build()
    }

    private fun notificationCount(): Int {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return shadowOf(manager).allNotifications.size
    }

    @Test
    fun `worker notifies only people due today and succeeds`() = runTest {
        val today = LocalDate.now()
        val due = today.plusDays(ReminderLead.ON_THE_DAY.days.toLong())
        val notDue = today.plusDays(20)
        val fakePersonRepo = FakePersonRepository().apply {
            setAll(
                listOf(
                    Person(id = 1L, name = "Due Today", birthMonth = due.monthValue, birthDay = due.dayOfMonth),
                    Person(id = 2L, name = "Not Due", birthMonth = notDue.monthValue, birthDay = notDue.dayOfMonth),
                ),
            )
        }
        val fakeSettingsRepo = FakeSettingsRepository(initialLead = ReminderLead.ON_THE_DAY)
        val worker = buildWorker(fakePersonRepo, fakeSettingsRepo)

        val result = worker.doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        assertEquals(1, notificationCount())
    }

    @Test
    fun `running the worker twice does not duplicate notifications`() = runTest {
        val today = LocalDate.now()
        val fakePersonRepo = FakePersonRepository().apply {
            setAll(
                listOf(
                    Person(id = 1L, name = "Due Today", birthMonth = today.monthValue, birthDay = today.dayOfMonth),
                ),
            )
        }
        val fakeSettingsRepo = FakeSettingsRepository(initialLead = ReminderLead.ON_THE_DAY)

        buildWorker(fakePersonRepo, fakeSettingsRepo).doWork()
        buildWorker(fakePersonRepo, fakeSettingsRepo).doWork()

        assertEquals(1, notificationCount())
    }
}
