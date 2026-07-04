package com.pocketaps.cakeday.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pocketaps.cakeday.core.common.dispatcher.DispatcherProvider
import com.pocketaps.cakeday.core.database.CakeDayyDatabase
import com.pocketaps.cakeday.core.database.entity.GroupEntity
import com.pocketaps.cakeday.core.database.entity.PersonEntity
import com.pocketaps.cakeday.core.domain.backup.ImportErrorReason
import com.pocketaps.cakeday.core.domain.backup.ImportResult
import com.pocketaps.cakeday.core.model.ReminderLead
import com.pocketaps.cakeday.core.model.ThemeMode
import com.pocketaps.cakeday.core.testing.fake.FakeSettingsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BackupRepositoryImplTest {

    private lateinit var db: CakeDayyDatabase
    private lateinit var fakeSettingsRepo: FakeSettingsRepository
    private lateinit var repository: BackupRepositoryImpl

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, CakeDayyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        fakeSettingsRepo = FakeSettingsRepository()
        val dispatchers = object : DispatcherProvider {
            override val io: CoroutineDispatcher = Dispatchers.Unconfined
            override val default: CoroutineDispatcher = Dispatchers.Unconfined
            override val main: CoroutineDispatcher = Dispatchers.Unconfined
        }
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        repository = BackupRepositoryImpl(db, fakeSettingsRepo, dispatchers, json)
    }

    @After
    fun tearDown() = db.close()

    private fun person(id: Long = 1L, name: String = "Alice") =
        PersonEntity(id = id, name = name, birthMonth = 5, birthDay = 10, createdAt = 1_000L, updatedAt = 1_000L)

    @Test
    fun `export then import round-trips people, groups, and settings including sync fields`() = runBlocking {
        db.personDao().upsert(person(id = 1L, name = "Alice").copy(remoteId = "remote-1"))
        db.personDao().upsert(person(id = 2L, name = "Deleted"))
        db.personDao().softDelete(2L, updatedAt = 1_500L)
        db.groupDao().upsert(
            GroupEntity(id = 1L, name = "Family", colorHex = "#F44336", createdAt = 1_000L, updatedAt = 1_000L),
        )
        fakeSettingsRepo.setReminderLead(ReminderLead.ONE_WEEK_BEFORE)
        fakeSettingsRepo.setThemeMode(ThemeMode.DARK)

        val json = repository.exportToJson()

        // Mutate everything after exporting so the import can prove it actually restores the export.
        db.replaceAllData(people = emptyList(), groups = emptyList())
        fakeSettingsRepo.setReminderLead(ReminderLead.ON_THE_DAY)
        fakeSettingsRepo.setThemeMode(ThemeMode.LIGHT)

        val result = repository.importFromJson(json)

        assertEquals(ImportResult.Success, result)
        val people = db.personDao().getAllIncludingDeleted()
        assertEquals(2, people.size)
        val alice = people.first { it.name == "Alice" }
        assertEquals("remote-1", alice.remoteId)
        assertEquals(1_000L, alice.updatedAt)
        val deleted = people.first { it.name == "Deleted" }
        assertTrue(deleted.isDeleted)
        val groups = db.groupDao().getAllIncludingDeleted()
        assertEquals(1, groups.size)
        assertEquals("Family", groups[0].name)
        assertEquals(ReminderLead.ONE_WEEK_BEFORE, fakeSettingsRepo.observeReminderLead().first())
    }

    @Test
    fun `importFromJson rejects malformed json and leaves existing data untouched`() = runBlocking {
        db.personDao().upsert(person())

        val result = repository.importFromJson("not valid json{{{")

        assertEquals(ImportResult.Error(ImportErrorReason.MALFORMED_JSON), result)
        assertEquals(1, db.personDao().getAllIncludingDeleted().size)
    }

    @Test
    fun `importFromJson rejects a newer schema version and leaves existing data untouched`() = runBlocking {
        db.personDao().upsert(person())
        val futureJson = """
            {"schemaVersion":999,"exportedAt":1000,"people":[],"groups":[],
             "settings":{"reminderLeadDays":0,"themeMode":"SYSTEM"}}
        """.trimIndent()

        val result = repository.importFromJson(futureJson)

        assertEquals(ImportResult.Error(ImportErrorReason.UNSUPPORTED_SCHEMA_VERSION), result)
        assertEquals(1, db.personDao().getAllIncludingDeleted().size)
    }

    @Test
    fun `importFromJson rejects a payload with duplicate ids and leaves existing data untouched`() = runBlocking {
        db.personDao().upsert(person())
        val duplicateIdJson = """
            {"schemaVersion":1,"exportedAt":1000,
             "people":[
               {"id":5,"name":"A","birthMonth":1,"birthDay":1,"createdAt":1000,"updatedAt":1000},
               {"id":5,"name":"B","birthMonth":2,"birthDay":2,"createdAt":1000,"updatedAt":1000}
             ],
             "groups":[],"settings":{"reminderLeadDays":0,"themeMode":"SYSTEM"}}
        """.trimIndent()

        val result = repository.importFromJson(duplicateIdJson)

        assertEquals(ImportResult.Error(ImportErrorReason.INVALID_PAYLOAD), result)
        val people = db.personDao().getAllIncludingDeleted()
        assertEquals(1, people.size)
        assertEquals("Alice", people[0].name)
    }
}
