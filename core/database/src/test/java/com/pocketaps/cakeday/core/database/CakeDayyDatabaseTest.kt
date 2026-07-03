package com.pocketaps.cakeday.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pocketaps.cakeday.core.database.entity.GroupEntity
import com.pocketaps.cakeday.core.database.entity.PersonEntity
import kotlinx.coroutines.flow.first
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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CakeDayyDatabaseTest {

    private lateinit var db: CakeDayyDatabase

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, CakeDayyDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun `deleteGroupAndUnassignPeople soft-deletes the group and nulls groupId without deleting people`() =
        runBlocking {
            db.groupDao().upsert(
                GroupEntity(id = 1L, name = "Family", colorHex = "#F44336", createdAt = 1_000L, updatedAt = 1_000L),
            )
            db.personDao().upsert(
                PersonEntity(
                    id = 1L,
                    name = "Alice",
                    birthMonth = 5,
                    birthDay = 10,
                    groupId = 1L,
                    createdAt = 1_000L,
                    updatedAt = 1_000L,
                ),
            )

            db.deleteGroupAndUnassignPeople(groupId = 1L, updatedAt = 2_000L)

            assertTrue(db.groupDao().observeAll().first().isEmpty())
            val people = db.personDao().observeAll().first()
            assertEquals(1, people.size)
            assertEquals("Alice", people[0].name)
            assertNull(people[0].groupId)
        }
}
