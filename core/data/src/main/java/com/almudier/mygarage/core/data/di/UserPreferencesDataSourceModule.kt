package com.almudier.mygarage.core.data.di

import com.almudier.mygarage.core.data.datastore.UserPreferencesDataSource
import com.almudier.mygarage.core.data.datastore.UserPreferencesDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserPreferencesDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindUserPreferencesDataSource(
        userPreferencesDataSourceImpl: UserPreferencesDataSourceImpl,
    ): UserPreferencesDataSource
}
