package com.pocketaps.cakeday.core.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.pocketaps.cakeday.core.common.dispatcher.DispatcherProvider
import com.pocketaps.cakeday.core.database.CakeDayyDatabase
import com.pocketaps.cakeday.core.model.Person
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PersonRepositoryImplTest {

    private lateinit var db: CakeDayyDatabase
    private lateinit var repository: PersonRepositoryImpl

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, CakeDayyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val dispatchers = object : DispatcherProvider {
            override val io: CoroutineDispatcher = Dispatchers.Unconfined
            override val default: CoroutineDispatcher = Dispatchers.Unconfined
            override val main: CoroutineDispatcher = Dispatchers.Unconfined
        }
        repository = PersonRepositoryImpl(db.personDao(), dispatchers)
    }

    @After
    fun tearDown() = db.close()

    private fun person(
        id: Long = 0,
        name: String = "Alice",
        month: Int = 6,
        day: Int = 15,
        birthYear: Int? = 1990,
    ) = Person(id = id, name = name, birthMonth = month, birthDay = day, birthYear = birthYear)

    @Test
    fun `upsert then observeAll returns mapped domain model`() = runBlocking {
        repository.upsert(person(name = "Alice", month = 3, day = 10, birthYear = 1990))
        repository.observeAll().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Alice", result[0].name)
            assertEquals(3, result[0].birthMonth)
            assertEquals(10, result[0].birthDay)
            assertEquals(1990, result[0].birthYear)
            cancel()
        }
    }

    @Test
    fun `delete removes person from future emissions`() = runBlocking {
        repository.upsert(person(name = "Alice"))
        repository.observeAll().test {
            val inserted = awaitItem()
            assertEquals(1, inserted.size)
            repository.delete(inserted[0].id)
            assertTrue(awaitItem().isEmpty())
            cancel()
        }
    }

    @Test
    fun `null birthYear round-trips correctly`() = runBlocking {
        repository.upsert(person(name = "Unknown", birthYear = null))
        repository.observeAll().test {
            assertNull(awaitItem()[0].birthYear)
            cancel()
        }
    }

    @Test
    fun `createdAt is preserved on update`() = runBlocking {
        repository.upsert(person(name = "Alice"))
        var original: Person? = null
        repository.observeAll().test {
            original = awaitItem()[0]
            cancel()
        }
        val captured = checkNotNull(original)
        repository.upsert(captured.copy(name = "Alicia"))
        repository.observeAll().test {
            assertEquals(captured.createdAt, awaitItem()[0].createdAt)
            cancel()
        }
    }

    @Test
    fun `observeUpcoming excludes people beyond withinDays`() = runBlocking {
        val today = LocalDate.now()
        val soon = today.plusDays(3)
        val later = today.plusDays(30)
        repository.upsert(person(name = "Soon", month = soon.monthValue, day = soon.dayOfMonth, birthYear = null))
        repository.upsert(person(name = "Later", month = later.monthValue, day = later.dayOfMonth, birthYear = null))
        repository.observeUpcoming(7).test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals("Soon", result[0].name)
            cancel()
        }
    }
}
