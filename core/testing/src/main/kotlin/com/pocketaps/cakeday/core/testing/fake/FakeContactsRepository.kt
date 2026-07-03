package com.pocketaps.cakeday.core.testing.fake

import com.pocketaps.cakeday.core.domain.repository.ContactsRepository
import com.pocketaps.cakeday.core.model.ContactBirthday

class FakeContactsRepository(
    private var contacts: List<ContactBirthday> = emptyList(),
) : ContactsRepository {

    override suspend fun fetchContactsWithBirthdays(): List<ContactBirthday> = contacts

    fun setContacts(value: List<ContactBirthday>) {
        contacts = value
    }
}
