package com.pocketaps.cakeday.core.database.migration

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Exercises [MIGRATION_1_2] directly against a raw v1 `person` table (schema taken from the
 * exported v1 Room schema) rather than via Room's MigrationTestHelper, whose driver-bridging
 * layer does not currently work under Robolectric on Windows.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MigrationsTest {

    private lateinit var helper: SupportSQLiteOpenHelper

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(TEST_DB)
        val configuration = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(TEST_DB)
            .callback(
                object : SupportSQLiteOpenHelper.Callback(1) {
                    override fun onCreate(db: SupportSQLiteDatabase) = Unit
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                },
            )
            .build()
        helper = FrameworkSQLiteOpenHelperFactory().create(configuration)
    }

    @After
    fun tearDown() = helper.close()

    @Test
    fun migrate1To2_addsGroupsTableWithoutTouchingExistingPersonData() {
        val db = helper.writableDatabase
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `person` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`remoteId` TEXT, `name` TEXT NOT NULL, `birthMonth` INTEGER NOT NULL, `birthDay` INTEGER NOT NULL, " +
                "`birthYear` INTEGER, `note` TEXT, `groupId` INTEGER, `reminderLeadDaysOverride` INTEGER, " +
                "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `isDeleted` INTEGER NOT NULL)",
        )
        db.execSQL(
            "INSERT INTO person (name, birthMonth, birthDay, createdAt, updatedAt, isDeleted) " +
                "VALUES ('Alice', 5, 10, 1000, 1000, 0)",
        )

        MIGRATION_1_2.migrate(db)

        val personCursor = db.query("SELECT name FROM person")
        assertTrue(personCursor.moveToFirst())
        assertEquals("Alice", personCursor.getString(0))
        personCursor.close()

        val groupsTableCursor = db.query("SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'groups'")
        assertTrue(groupsTableCursor.moveToFirst())
        groupsTableCursor.close()
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
    }
}
