package com.pocketaps.cakeday.widget.di

import com.pocketaps.cakeday.core.domain.usecase.GetUpcomingBirthdaysUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CakeDayyWidgetEntryPoint {
    fun getUpcomingBirthdaysUseCase(): GetUpcomingBirthdaysUseCase
}
