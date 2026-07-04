package com.pocketaps.cakeday.feature.settings.backup

import app.cash.turbine.test
import com.pocketaps.cakeday.core.domain.backup.ImportErrorReason
import com.pocketaps.cakeday.core.domain.backup.ImportResult
import com.pocketaps.cakeday.core.domain.usecase.ExportDataUseCase
import com.pocketaps.cakeday.core.domain.usecase.ImportDataUseCase
import com.pocketaps.cakeday.core.testing.fake.FakeBackupRepository
import com.pocketaps.cakeday.core.testing.rule.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class BackupViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(fakeRepo: FakeBackupRepository): BackupViewModel = BackupViewModel(
        exportDataUseCase = ExportDataUseCase(fakeRepo),
        importDataUseCase = ImportDataUseCase(fakeRepo),
    )

    @Test
    fun `buildExportJson returns the repository's json and sets Working state`() = runTest {
        val fakeRepo = FakeBackupRepository(exportResult = """{"schemaVersion":1}""")
        val viewModel = createViewModel(fakeRepo)

        viewModel.uiState.test {
            assertEquals(BackupUiState.Idle, awaitItem())
            val json = viewModel.buildExportJson()
            assertEquals(BackupUiState.Working, awaitItem())
            assertEquals("""{"schemaVersion":1}""", json)
        }
    }

    @Test
    fun `onJsonPicked with a successful import result emits ImportSuccess`() = runTest {
        val fakeRepo = FakeBackupRepository(importResult = ImportResult.Success)
        val viewModel = createViewModel(fakeRepo)

        // With the UnconfinedTestDispatcher from MainDispatcherRule, onJsonPicked's launched
        // coroutine runs synchronously to completion with no real suspension point, so the
        // transient Working state is conflated away before the collector can observe it.
        viewModel.uiState.test {
            assertEquals(BackupUiState.Idle, awaitItem())
            viewModel.onJsonPicked("""{"schemaVersion":1}""")
            assertEquals(BackupUiState.ImportSuccess, awaitItem())
        }
    }

    @Test
    fun `onJsonPicked with a malformed-json error emits Error MALFORMED_JSON`() = runTest {
        val fakeRepo = FakeBackupRepository(importResult = ImportResult.Error(ImportErrorReason.MALFORMED_JSON))
        val viewModel = createViewModel(fakeRepo)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onJsonPicked("not json")
            assertEquals(BackupUiState.Error(BackupErrorReason.MALFORMED_JSON), awaitItem())
        }
    }

    @Test
    fun `onJsonPicked with an unsupported-schema error emits Error UNSUPPORTED_SCHEMA_VERSION`() = runTest {
        val fakeRepo = FakeBackupRepository(
            importResult = ImportResult.Error(ImportErrorReason.UNSUPPORTED_SCHEMA_VERSION),
        )
        val viewModel = createViewModel(fakeRepo)

        viewModel.uiState.test {
            awaitItem()
            viewModel.onJsonPicked("""{"schemaVersion":999}""")
            assertEquals(BackupUiState.Error(BackupErrorReason.UNSUPPORTED_SCHEMA_VERSION), awaitItem())
        }
    }

    @Test
    fun `onFileReadFailed emits Error FILE_READ_WRITE_FAILED`() = runTest {
        val viewModel = createViewModel(FakeBackupRepository())

        viewModel.uiState.test {
            assertEquals(BackupUiState.Idle, awaitItem())
            viewModel.onFileReadFailed()
            assertEquals(BackupUiState.Error(BackupErrorReason.FILE_READ_WRITE_FAILED), awaitItem())
        }
    }
}
