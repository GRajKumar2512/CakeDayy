package com.pocketaps.cakeday.core.domain.usecase

import com.pocketaps.cakeday.core.domain.repository.PersonRepository
import javax.inject.Inject

class DeletePersonUseCase @Inject constructor(
    private val personRepository: PersonRepository,
) {
    suspend operator fun invoke(id: Long) {
        personRepository.delete(id)
    }
}
