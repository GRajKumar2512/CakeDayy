package com.pocketaps.cakeday.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.pocketaps.cakeday.core.database.CakeDayyDatabase
import com.pocketaps.cakeday.core.database.entity.GroupEntity
import kotlinx.coroutines.runBlocking
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
class GroupDaoTest {

    private lateinit var db: CakeDayyDatabase
    private lateinit var dao: GroupDao

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, CakeDayyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.groupDao()
    }

    @After
    fun tearDown() = db.close()

    private fun entity(id: Long = 0, name: String = "Family", colorHex: String = "#F44336") =
        GroupEntity(id = id, name = name, colorHex = colorHex, createdAt = 1_000L, updatedAt = 1_000L)

    @Test
    fun `observeAll emits newly inserted entity`() = runBlocking {
        dao.upsert(entity(name = "Family"))
        dao.observeAll().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Family", result[0].name)
            cancel()
        }
    }

    @Test
    fun `upsert with same id updates record in place`() = runBlocking {
        dao.upsert(entity(id = 1, name = "Family"))
        dao.upsert(entity(id = 1, name = "Work"))
        dao.observeAll().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Work", result[0].name)
            cancel()
        }
    }

    @Test
    fun `softDelete hides entity from observeAll`() = runBlocking {
        dao.upsert(entity(id = 1, name = "Family"))
        dao.softDelete(1, updatedAt = 2_000L)
        dao.observeAll().test {
            assertTrue(awaitItem().isEmpty())
            cancel()
        }
    }

    @Test
    fun `observeAll orders by name`() = runBlocking {
        dao.upsert(entity(name = "Work"))
        dao.upsert(entity(name = "Family"))
        dao.observeAll().test {
            val result = awaitItem()
            assertEquals(listOf("Family", "Work"), result.map { it.name })
            cancel()
        }
    }

    @Test
    fun `getAllIncludingDeleted includes soft-deleted rows`() = runBlocking {
        dao.upsert(entity(id = 1, name = "Family"))
        dao.upsert(entity(id = 2, name = "Work"))
        dao.softDelete(2, updatedAt = 2_000L)

        val all = dao.getAllIncludingDeleted()

        assertEquals(2, all.size)
        assertTrue(all.any { it.name == "Work" && it.isDeleted })
    }

    @Test
    fun `deleteAll empties the table`() = runBlocking {
        dao.upsert(entity(id = 1, name = "Family"))
        dao.deleteAll()
        assertTrue(dao.getAllIncludingDeleted().isEmpty())
    }

    @Test
    fun `insertAll bulk-inserts with explicit ids preserved`() = runBlocking {
        dao.insertAll(listOf(entity(id = 1, name = "Family"), entity(id = 2, name = "Work")))

        val all = dao.getAllIncludingDeleted()

        assertEquals(2, all.size)
        assertEquals("Family", all.first { it.id == 1L }.name)
        assertEquals("Work", all.first { it.id == 2L }.name)
    }
}
