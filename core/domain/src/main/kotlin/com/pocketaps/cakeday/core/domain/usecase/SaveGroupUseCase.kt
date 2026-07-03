package com.pocketaps.cakeday.core.domain.usecase

import com.pocketaps.cakeday.core.domain.repository.GroupRepository
import com.pocketaps.cakeday.core.model.Group
import javax.inject.Inject

class SaveGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository,
) {
    suspend operator fun invoke(group: Group) {
        groupRepository.upsertGroup(group)
    }
}
