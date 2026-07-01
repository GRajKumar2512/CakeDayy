package com.pocketaps.cakeday.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.pocketaps.cakeday.core.database.CakeDayyDatabase
import com.pocketaps.cakeday.core.database.entity.PersonEntity
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
class PersonDaoTest {

    private lateinit var db: CakeDayyDatabase
    private lateinit var dao: PersonDao

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, CakeDayyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.personDao()
    }

    @After
    fun tearDown() = db.close()

    private fun entity(id: Long = 0, name: String = "Alice", month: Int = 6, day: Int = 15) =
        PersonEntity(id = id, name = name, birthMonth = month, birthDay = day, createdAt = 1_000L, updatedAt = 1_000L)

    @Test
    fun `observeAll emits newly inserted entity`() = runBlocking {
        dao.upsert(entity(name = "Alice"))
        dao.observeAll().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Alice", result[0].name)
            cancel()
        }
    }

    @Test
    fun `upsert with same id updates record in place`() = runBlocking {
        dao.upsert(entity(id = 1, name = "Alice"))
        dao.upsert(entity(id = 1, name = "Alicia"))
        dao.observeAll().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Alicia", result[0].name)
            cancel()
        }
    }

    @Test
    fun `softDelete hides entity from observeAll`() = runBlocking {
        dao.upsert(entity(id = 1, name = "Alice"))
        dao.softDelete(1, updatedAt = 2_000L)
        dao.observeAll().test {
            assertTrue(awaitItem().isEmpty())
            cancel()
        }
    }

    @Test
    fun `observeAll emits on each data change`() = runBlocking {
        dao.observeAll().test {
            assertEquals(0, awaitItem().size)
            dao.upsert(entity(name = "First"))
            assertEquals(1, awaitItem().size)
            dao.upsert(entity(name = "Second"))
            assertEquals(2, awaitItem().size)
            cancel()
        }
    }
}
