package com.pocketaps.cakeday.core.domain.usecase

import com.pocketaps.cakeday.core.domain.repository.PersonRepository
import com.pocketaps.cakeday.core.domain.util.BirthdayDateUtils
import com.pocketaps.cakeday.core.model.UpcomingBirthday
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetUpcomingBirthdaysUseCase @Inject constructor(
    private val personRepository: PersonRepository,
) {
    operator fun invoke(): Flow<List<UpcomingBirthday>> =
        personRepository.observeAll().map { people ->
            val today = LocalDate.now()
            people
                .map { person ->
                    val nextBirthday = BirthdayDateUtils.nextBirthdayDate(
                        person.birthMonth,
                        person.birthDay,
                        today,
                    )
                    UpcomingBirthday(
                        person = person,
                        daysUntilNext = ChronoUnit.DAYS.between(today, nextBirthday).toInt(),
                        nextAge = person.birthYear?.let {
                            BirthdayDateUtils.ageOnNextBirthday(it, nextBirthday)
                        },
                    )
                }
                .sortedBy { it.daysUntilNext }
        }
}
