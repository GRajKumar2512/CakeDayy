package com.pocketaps.cakeday.core.domain.usecase

import com.pocketaps.cakeday.core.model.Person
import com.pocketaps.cakeday.core.model.ReminderLead
import com.pocketaps.cakeday.core.testing.fake.FakePersonRepository
import com.pocketaps.cakeday.core.testing.fake.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetDueRemindersUseCaseTest {

    private lateinit var fakePersonRepo: FakePersonRepository
    private lateinit var fakeSettingsRepo: FakeSettingsRepository
    private lateinit var useCase: GetDueRemindersUseCase

    @Before
    fun setUp() {
        fakePersonRepo = FakePersonRepository()
        fakeSettingsRepo = FakeSettingsRepository(initialLead = ReminderLead.ONE_WEEK_BEFORE)
        useCase = GetDueRemindersUseCase(GetUpcomingBirthdaysUseCase(fakePersonRepo), fakeSettingsRepo)
    }

    @Test
    fun `person due under the global lead is included`() = runTest {
        val target = LocalDate.now().plusDays(7)
        fakePersonRepo.setAll(
            listOf(Person(id = 1L, name = "Alice", birthMonth = target.monthValue, birthDay = target.dayOfMonth)),
        )

        val due = useCase()

        assertEquals(1, due.size)
        assertEquals("Alice", due[0].person.name)
    }

    @Test
    fun `person not due under the global lead is excluded`() = runTest {
        val target = LocalDate.now().plusDays(2)
        fakePersonRepo.setAll(
            listOf(Person(id = 1L, name = "Alice", birthMonth = target.monthValue, birthDay = target.dayOfMonth)),
        )

        val due = useCase()

        assertEquals(emptyList<Any>(), due)
    }

    @Test
    fun `per-person override beats the global lead`() = runTest {
        val target = LocalDate.now().plusDays(2)
        fakePersonRepo.setAll(
            listOf(
                Person(
                    id = 1L,
                    name = "Alice",
                    birthMonth = target.monthValue,
                    birthDay = target.dayOfMonth,
                    reminderLeadDaysOverride = 2,
                ),
            ),
        )

        val due = useCase()

        assertEquals(1, due.size)
        assertEquals("Alice", due[0].person.name)
    }
}
