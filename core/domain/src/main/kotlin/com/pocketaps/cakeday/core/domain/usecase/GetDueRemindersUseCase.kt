package com.pocketaps.cakeday.core.domain.usecase

import com.pocketaps.cakeday.core.domain.repository.SettingsRepository
import com.pocketaps.cakeday.core.domain.util.ReminderRules
import com.pocketaps.cakeday.core.model.UpcomingBirthday
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

class GetDueRemindersUseCase @Inject constructor(
    private val getUpcomingBirthdays: GetUpcomingBirthdaysUseCase,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(): List<UpcomingBirthday> {
        val globalLead = settingsRepository.observeReminderLead().first()
        val today = LocalDate.now()
        return getUpcomingBirthdays().first().filter { upcoming ->
            val effectiveLead = ReminderRules.effectiveLeadDays(upcoming.person, globalLead)
            ReminderRules.isReminderDue(upcoming.person, today, effectiveLead)
        }
    }
}
