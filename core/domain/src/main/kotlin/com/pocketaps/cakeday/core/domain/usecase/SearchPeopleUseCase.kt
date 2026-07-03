package com.pocketaps.cakeday.core.domain.usecase

import com.pocketaps.cakeday.core.model.UpcomingBirthday
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchPeopleUseCase @Inject constructor(
    private val getUpcomingBirthdays: GetUpcomingBirthdaysUseCase,
) {
    operator fun invoke(query: String, groupId: Long?): Flow<List<UpcomingBirthday>> =
        getUpcomingBirthdays().map { people -> people.filter { matches(it, query, groupId) } }

    private fun matches(upcoming: UpcomingBirthday, query: String, groupId: Long?): Boolean {
        val queryMatches = query.isBlank() ||
            upcoming.person.name.contains(query, ignoreCase = true) ||
            upcoming.person.note?.contains(query, ignoreCase = true) == true
        val groupMatches = groupId == null || upcoming.person.groupId == groupId
        return queryMatches && groupMatches
    }
}
