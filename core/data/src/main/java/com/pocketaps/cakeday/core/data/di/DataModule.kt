package com.pocketaps.cakeday.core.data.di

import com.pocketaps.cakeday.core.common.dispatcher.DefaultDispatcherProvider
import com.pocketaps.cakeday.core.common.dispatcher.DispatcherProvider
import com.pocketaps.cakeday.core.data.repository.BackupRepositoryImpl
import com.pocketaps.cakeday.core.data.repository.ContactsRepositoryImpl
import com.pocketaps.cakeday.core.data.repository.GroupRepositoryImpl
import com.pocketaps.cakeday.core.data.repository.PersonRepositoryImpl
import com.pocketaps.cakeday.core.data.repository.SettingsRepositoryImpl
import com.pocketaps.cakeday.core.domain.repository.BackupRepository
import com.pocketaps.cakeday.core.domain.repository.ContactsRepository
import com.pocketaps.cakeday.core.domain.repository.GroupRepository
import com.pocketaps.cakeday.core.domain.repository.PersonRepository
import com.pocketaps.cakeday.core.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindPersonRepository(impl: PersonRepositoryImpl): PersonRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindGroupRepository(impl: GroupRepositoryImpl): GroupRepository

    @Binds
    @Singleton
    abstract fun bindContactsRepository(impl: ContactsRepositoryImpl): ContactsRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository

    companion object {

        @Provides
        @Singleton
        fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

        @Provides
        @Singleton
        fun provideJson(): Json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
    }
}
