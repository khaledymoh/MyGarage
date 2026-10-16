package com.almudier.mygarage.authentication.domain.usecase

import android.content.Context
import com.almudier.mygarage.authentication.domain.model.AuthUser
import com.almudier.mygarage.authentication.domain.repository.AuthRepository
import javax.inject.Inject

class SignInWithGoogleUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(activityContext: Context): Result<AuthUser> {
        return repository.signInWithGoogle(activityContext)
    }
}
