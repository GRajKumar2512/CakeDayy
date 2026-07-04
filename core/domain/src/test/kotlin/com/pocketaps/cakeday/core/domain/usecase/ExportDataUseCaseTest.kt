package com.pocketaps.cakeday.core.domain.usecase

import com.pocketaps.cakeday.core.testing.fake.FakeBackupRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ExportDataUseCaseTest {

    @Test
    fun `invoke delegates to the repository's exported json`() = runTest {
        val fakeRepo = FakeBackupRepository(exportResult = """{"schemaVersion":1}""")
        val useCase = ExportDataUseCase(fakeRepo)

        val result = useCase()

        assertEquals("""{"schemaVersion":1}""", result)
    }
}
