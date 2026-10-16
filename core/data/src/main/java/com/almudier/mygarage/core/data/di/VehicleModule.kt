package com.almudier.mygarage.core.data.di

import com.almudier.mygarage.core.data.repository.MaintenanceServiceRepositoryImpl
import com.almudier.mygarage.core.data.repository.AppSettingsRepositoryImpl
import com.almudier.mygarage.core.data.repository.MaintenanceRepositoryImpl
import com.almudier.mygarage.core.data.repository.VehicleRepositoryImpl
import com.almudier.mygarage.core.domain.repository.AppSettingsRepository
import com.almudier.mygarage.core.domain.repository.MaintenanceRepository
import com.almudier.mygarage.core.domain.repository.MaintenanceServiceRepository
import com.almudier.mygarage.core.domain.repository.VehicleRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VehicleBindsModule {

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(impl: VehicleRepositoryImpl): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindMaintenanceRepository(impl: MaintenanceRepositoryImpl): MaintenanceRepository

    @Binds
    @Singleton
    abstract fun bindMaintenanceServiceRepository(impl: MaintenanceServiceRepositoryImpl): MaintenanceServiceRepository

    @Binds
    @Singleton
    abstract fun bindAppSettingsRepository(impl: AppSettingsRepositoryImpl): AppSettingsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}
