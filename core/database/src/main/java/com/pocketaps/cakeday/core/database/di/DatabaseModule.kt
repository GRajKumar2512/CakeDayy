package com.pocketaps.cakeday.core.database.di

import android.content.Context
import androidx.room.Room
import com.pocketaps.cakeday.core.database.CakeDayyDatabase
import com.pocketaps.cakeday.core.database.dao.PersonDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CakeDayyDatabase =
        Room.databaseBuilder(context, CakeDayyDatabase::class.java, "cakeday.db").build()

    @Provides
    @Singleton
    fun providePersonDao(database: CakeDayyDatabase): PersonDao = database.personDao()
}
