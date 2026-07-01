package com.pocketaps.cakeday.core.domain.usecase

import com.pocketaps.cakeday.core.domain.repository.PersonRepository
import com.pocketaps.cakeday.core.model.Person
import javax.inject.Inject

class SavePersonUseCase @Inject constructor(
    private val personRepository: PersonRepository,
) {
    suspend operator fun invoke(person: Person) {
        personRepository.upsert(person)
    }
}
