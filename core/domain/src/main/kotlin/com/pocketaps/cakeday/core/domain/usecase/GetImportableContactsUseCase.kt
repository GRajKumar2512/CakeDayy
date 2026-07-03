package com.pocketaps.cakeday.core.domain.usecase

import com.pocketaps.cakeday.core.domain.repository.ContactsRepository
import com.pocketaps.cakeday.core.domain.repository.PersonRepository
import com.pocketaps.cakeday.core.model.ContactBirthday
import com.pocketaps.cakeday.core.model.Person
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetImportableContactsUseCase @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val personRepository: PersonRepository,
) {
    suspend operator fun invoke(): List<ContactBirthday> {
        val existingKeys = personRepository.observeAll().first().map { it.dedupeKey() }.toSet()
        return contactsRepository.fetchContactsWithBirthdays()
            .filterNot { it.dedupeKey() in existingKeys }
    }

    private fun Person.dedupeKey() = dedupeKey(name, birthMonth, birthDay, birthYear)

    private fun ContactBirthday.dedupeKey() = dedupeKey(name, birthMonth, birthDay, birthYear)

    private fun dedupeKey(name: String, month: Int, day: Int, year: Int?) =
        "${name.trim().lowercase()}|$month|$day|$year"
}
