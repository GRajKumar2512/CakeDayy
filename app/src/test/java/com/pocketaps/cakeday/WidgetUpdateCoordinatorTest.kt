package com.pocketaps.cakeday

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.pocketaps.cakeday.core.domain.usecase.GetUpcomingBirthdaysUseCase
import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.testing.fake.FakePersonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [WidgetUpdateCoordinator.refreshTriggers] is tested by driving its [isDailyJobRunning] seam
 * directly rather than a real WorkManager instance, since real WorkManager state transitions
 * under Robolectric happen on background executors with no deterministic way to observe the
 * transient RUNNING state from a test. The seam exists for exactly this reason; production code
 * always calls it with the default argument backed by the real WorkManager flow.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WidgetUpdateCoordinatorTest {

    private lateinit var fakeRepo: FakePersonRepository
    private lateinit var coordinator: WidgetUpdateCoordinator

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        fakeRepo = FakePersonRepository()
        coordinator = WidgetUpdateCoordinator(context, GetUpcomingBirthdaysUseCase(fakeRepo))
    }

    @Test
    fun `initial subscription emits once and does not double-fire from the daily job's starting state`() = runTest {
        val isDailyJobRunning = MutableStateFlow(false)

        coordinator.refreshTriggers(isDailyJobRunning).test {
            awaitItem() // initial emission from the current data
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a change to the observed data triggers a refresh signal`() = runTest {
        val isDailyJobRunning = MutableStateFlow(false)

        coordinator.refreshTriggers(isDailyJobRunning).test {
            awaitItem() // initial emission from the current (empty) data

            fakeRepo.setAll(listOf(Person(id = 1L, name = "Alice", birthMonth = 5, birthDay = 10)))

            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `the daily job finishing triggers a refresh signal via the RUNNING to not-RUNNING falling edge`() = runTest {
        val isDailyJobRunning = MutableStateFlow(false)

        coordinator.refreshTriggers(isDailyJobRunning).test {
            awaitItem() // initial emission from the current data

            isDailyJobRunning.value = true
            expectNoEvents() // the rising edge alone must not trigger a refresh

            isDailyJobRunning.value = false

            awaitItem() // the falling edge does
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `a job that is already running when the app starts does not spuriously trigger on subscription`() = runTest {
        val isDailyJobRunning = MutableStateFlow(true)

        coordinator.refreshTriggers(isDailyJobRunning).test {
            awaitItem() // initial emission from the current data
            expectNoEvents() // the pre-existing RUNNING state must be dropped, not treated as a transition

            isDailyJobRunning.value = false

            awaitItem() // now the falling edge fires
            cancelAndIgnoreRemainingEvents()
        }
    }
}
