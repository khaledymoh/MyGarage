package com.almudier.mygarage.authentication.data.di

import com.almudier.mygarage.authentication.api.SignOutUseCase
import com.almudier.mygarage.authentication.domain.usecase.SignOutUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthUseCaseModule {

    @Binds
    abstract fun bindSignOutUseCase(impl: SignOutUseCaseImpl): SignOutUseCase
}
