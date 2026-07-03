package com.pocketaps.cakeday.core.domain.usecase

import app.cash.turbine.test
import com.pocketaps.cakeday.core.model.Group
import com.pocketaps.cakeday.core.testing.fake.FakeGroupRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveGroupsUseCaseTest {

    @Test
    fun `emits groups from the repository`() = runTest {
        val fakeRepo = FakeGroupRepository()
        fakeRepo.setAll(listOf(Group(id = 1L, name = "Family", colorHex = "#F44336")))
        val useCase = ObserveGroupsUseCase(fakeRepo)

        useCase().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Family", result[0].name)
        }
    }
}
