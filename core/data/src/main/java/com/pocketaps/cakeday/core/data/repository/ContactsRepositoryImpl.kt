package com.pocketaps.cakeday.core.data.repository

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import com.pocketaps.cakeday.core.common.dispatcher.DispatcherProvider
import com.pocketaps.cakeday.core.domain.repository.ContactsRepository
import com.pocketaps.cakeday.core.domain.util.ContactEventDateParser
import com.pocketaps.cakeday.core.model.ContactBirthday
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ContactsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider,
) : ContactsRepository {

    override suspend fun fetchContactsWithBirthdays(): List<ContactBirthday> = withContext(dispatchers.io) {
        val results = mutableListOf<ContactBirthday>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Event.CONTACT_ID,
            ContactsContract.CommonDataKinds.Event.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Event.START_DATE,
        )
        val selection = "${ContactsContract.Data.MIMETYPE} = ? AND " +
            "${ContactsContract.CommonDataKinds.Event.TYPE} = ?"
        val selectionArgs = arrayOf(
            ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
            ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString(),
        )

        context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null,
        )?.use { cursor ->
            val columns = EventColumns(
                idIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.CONTACT_ID),
                nameIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.DISPLAY_NAME),
                dateIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Event.START_DATE),
            )
            while (cursor.moveToNext()) {
                cursor.toContactBirthdayOrNull(columns)?.let { results += it }
            }
        }

        results
    }

    private data class EventColumns(val idIndex: Int, val nameIndex: Int, val dateIndex: Int)

    private fun Cursor.toContactBirthdayOrNull(columns: EventColumns): ContactBirthday? {
        val name = getString(columns.nameIndex)
        val parsed = getString(columns.dateIndex)?.let(ContactEventDateParser::parse)
        return if (name != null && parsed != null) {
            ContactBirthday(
                contactId = getString(columns.idIndex),
                name = name,
                birthMonth = parsed.month,
                birthDay = parsed.day,
                birthYear = parsed.year,
            )
        } else {
            null
        }
    }
}
