package com.pocketaps.cakeday.core.domain.usecase

import com.pocketaps.cakeday.core.domain.backup.ImportErrorReason
import com.pocketaps.cakeday.core.domain.backup.ImportResult
import com.pocketaps.cakeday.core.testing.fake.FakeBackupRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ImportDataUseCaseTest {

    @Test
    fun `invoke passes the json through and returns the repository's success result`() = runTest {
        val fakeRepo = FakeBackupRepository(importResult = ImportResult.Success)
        val useCase = ImportDataUseCase(fakeRepo)

        val result = useCase("""{"schemaVersion":1}""")

        assertEquals(ImportResult.Success, result)
        assertEquals("""{"schemaVersion":1}""", fakeRepo.lastImportedJson)
    }

    @Test
    fun `invoke returns the repository's error result unchanged`() = runTest {
        val fakeRepo = FakeBackupRepository(importResult = ImportResult.Error(ImportErrorReason.MALFORMED_JSON))
        val useCase = ImportDataUseCase(fakeRepo)

        val result = useCase("not json")

        assertEquals(ImportResult.Error(ImportErrorReason.MALFORMED_JSON), result)
    }
}
