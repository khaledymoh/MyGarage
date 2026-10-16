package com.almudier.mygarage.authentication.domain.usecase

import com.almudier.mygarage.authentication.api.SignOutUseCase
import com.almudier.mygarage.authentication.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCaseImpl @Inject constructor(
    private val repository: AuthRepository,
) : SignOutUseCase {
    override suspend fun invoke() {
        repository.signOut()
    }
}
