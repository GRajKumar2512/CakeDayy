package com.pocketaps.cakeday.core.domain.repository

import com.pocketaps.cakeday.core.model.ContactBirthday

interface ContactsRepository {
    suspend fun fetchContactsWithBirthdays(): List<ContactBirthday>
}
