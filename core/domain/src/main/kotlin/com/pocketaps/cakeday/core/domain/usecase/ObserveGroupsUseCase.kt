package com.pocketaps.cakeday.core.domain.usecase

import com.pocketaps.cakeday.core.domain.repository.GroupRepository
import com.pocketaps.cakeday.core.model.Group
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveGroupsUseCase @Inject constructor(
    private val groupRepository: GroupRepository,
) {
    operator fun invoke(): Flow<List<Group>> = groupRepository.observeGroups()
}
