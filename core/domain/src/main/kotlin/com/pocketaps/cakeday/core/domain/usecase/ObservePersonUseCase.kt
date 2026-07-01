package com.pocketaps.cakeday.core.domain.usecase

import com.pocketaps.cakeday.core.domain.repository.PersonRepository
import com.pocketaps.cakeday.core.model.Person
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObservePersonUseCase @Inject constructor(
    private val personRepository: PersonRepository,
) {
    operator fun invoke(id: Long): Flow<Person?> =
        personRepository.observeAll().map { people -> people.find { it.id == id } }
}
