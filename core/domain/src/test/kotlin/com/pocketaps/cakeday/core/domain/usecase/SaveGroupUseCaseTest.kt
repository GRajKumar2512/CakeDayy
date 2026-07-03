package com.pocketaps.cakeday.core.domain.usecase

import app.cash.turbine.test
import com.pocketaps.cakeday.core.model.Group
import com.pocketaps.cakeday.core.testing.fake.FakeGroupRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SaveGroupUseCaseTest {

    @Test
    fun `creating a group persists it via the repository`() = runTest {
        val fakeRepo = FakeGroupRepository()
        val useCase = SaveGroupUseCase(fakeRepo)

        useCase(Group(name = "Family", colorHex = "#F44336"))

        fakeRepo.observeGroups().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Family", result[0].name)
        }
    }

    @Test
    fun `renaming an existing group updates it in place`() = runTest {
        val fakeRepo = FakeGroupRepository()
        fakeRepo.setAll(listOf(Group(id = 1L, name = "Family", colorHex = "#F44336")))
        val useCase = SaveGroupUseCase(fakeRepo)

        useCase(Group(id = 1L, name = "Friends", colorHex = "#2196F3"))

        fakeRepo.observeGroups().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Friends", result[0].name)
            assertEquals("#2196F3", result[0].colorHex)
        }
    }
}
