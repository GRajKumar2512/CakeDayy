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
import org.junit.Assert.assertThrows
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

    @Test
    fun `replaceAllData replaces existing people and groups with the given lists`() = runBlocking {
        db.personDao().upsert(
            PersonEntity(id = 1L, name = "Old", birthMonth = 1, birthDay = 1, createdAt = 1_000L, updatedAt = 1_000L),
        )
        db.groupDao().upsert(
            GroupEntity(id = 1L, name = "Old Group", colorHex = "#F44336", createdAt = 1_000L, updatedAt = 1_000L),
        )

        db.replaceAllData(
            people = listOf(
                PersonEntity(
                    id = 2L,
                    name = "New",
                    birthMonth = 5,
                    birthDay = 10,
                    createdAt = 2_000L,
                    updatedAt = 2_000L,
                ),
            ),
            groups = listOf(
                GroupEntity(id = 2L, name = "New Group", colorHex = "#2196F3", createdAt = 2_000L, updatedAt = 2_000L),
            ),
        )

        val people = db.personDao().getAllIncludingDeleted()
        val groups = db.groupDao().getAllIncludingDeleted()
        assertEquals(1, people.size)
        assertEquals("New", people[0].name)
        assertEquals(1, groups.size)
        assertEquals("New Group", groups[0].name)
    }

    @Test
    fun `replaceAllData rolls back entirely when a duplicate id causes insertAll to fail`() = runBlocking {
        db.personDao().upsert(
            PersonEntity(
                id = 1L,
                name = "Original",
                birthMonth = 1,
                birthDay = 1,
                createdAt = 1_000L,
                updatedAt = 1_000L,
            ),
        )

        val duplicateIdPeople = listOf(
            PersonEntity(id = 5L, name = "A", birthMonth = 2, birthDay = 2, createdAt = 2_000L, updatedAt = 2_000L),
            PersonEntity(id = 5L, name = "B", birthMonth = 3, birthDay = 3, createdAt = 2_000L, updatedAt = 2_000L),
        )

        assertThrows(Exception::class.java) {
            runBlocking { db.replaceAllData(people = duplicateIdPeople, groups = emptyList()) }
        }

        val people = db.personDao().getAllIncludingDeleted()
        assertEquals(1, people.size)
        assertEquals("Original", people[0].name)
    }
}
