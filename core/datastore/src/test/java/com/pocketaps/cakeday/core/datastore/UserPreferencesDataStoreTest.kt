package com.pocketaps.cakeday.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.pocketaps.cakeday.core.model.ReminderLead
import com.pocketaps.cakeday.core.model.ThemeMode
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

private val REMINDER_LEAD_DAYS = intPreferencesKey("reminder_lead_days")
private val THEME_MODE = stringPreferencesKey("theme_mode")

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UserPreferencesDataStoreTest {

    private lateinit var file: File
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var preferences: UserPreferencesDataStore

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        file = File(context.filesDir, "test-${System.nanoTime()}.preferences_pb")
        dataStore = PreferenceDataStoreFactory.create(produceFile = { file })
        preferences = UserPreferencesDataStore(dataStore)
    }

    @After
    fun tearDown() {
        file.delete()
    }

    @Test
    fun `reminderLead falls back to ON_THE_DAY when the stored value matches no known lead`() = runTest {
        dataStore.edit { it[REMINDER_LEAD_DAYS] = 999 }

        preferences.reminderLead.test {
            assertEquals(ReminderLead.ON_THE_DAY, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `reminderLead reflects a validly stored value`() = runTest {
        dataStore.edit { it[REMINDER_LEAD_DAYS] = ReminderLead.ONE_WEEK_BEFORE.days }

        preferences.reminderLead.test {
            assertEquals(ReminderLead.ONE_WEEK_BEFORE, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `themeMode falls back to SYSTEM when the stored value matches no known mode`() = runTest {
        dataStore.edit { it[THEME_MODE] = "NOT_A_REAL_MODE" }

        preferences.themeMode.test {
            assertEquals(ThemeMode.SYSTEM, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `themeMode reflects a validly stored value`() = runTest {
        dataStore.edit { it[THEME_MODE] = ThemeMode.DARK.name }

        preferences.themeMode.test {
            assertEquals(ThemeMode.DARK, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
