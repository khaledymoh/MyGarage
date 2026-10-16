package com.almudier.mygarage.authentication.data.remote

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import com.almudier.mygarage.authentication.data.mapper.toDto
import com.almudier.mygarage.authentication.data.model.UserDto
import com.almudier.mygarage.authentication.domain.model.AuthError
import com.almudier.mygarage.authentication.domain.model.AuthException
import com.almudier.mygarage.core.data.network.FirebaseNetworkCaller
import com.almudier.mygarage.resources.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject

class AuthRemoteDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val credentialManager: CredentialManager,
    private val firebaseAuth: FirebaseAuth,
    private val networkCaller: FirebaseNetworkCaller,
): AuthRemoteDataSource {

    override suspend fun getGoogleIdToken(activityContext: Context): Result<String> = runCatching {
        val option = GetSignInWithGoogleOption
            .Builder(appContext.getString(R.string.google_server_client_id))
            .setNonce(generateNonce())
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()

        val response = try {
            credentialManager.getCredential(activityContext, request)
        } catch (e: GetCredentialCancellationException) {
            throw AuthException(AuthError.Cancelled, e)
        } catch (e: NoCredentialException) {
            throw AuthException(AuthError.NoGoogleAccount, e)
        } catch (e: GetCredentialProviderConfigurationException) {
            throw AuthException(AuthError.NoPlayServices, e)
        } catch (e: GetCredentialException) {
            throw AuthException(AuthError.Unknown(e.message), e)
        }

        val credential = response.credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } else {
            throw AuthException(AuthError.Unknown("Unexpected credential type: ${credential.type}"))
        }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<UserDto> =
        networkCaller.call {
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(authCredential).await().user?.toDto()
                ?: throw AuthException(AuthError.Unknown("Firebase returned a null user"))
        }

    override suspend fun signOut() {
        firebaseAuth.signOut()
        runCatching {
            credentialManager.clearCredentialState(ClearCredentialStateRequest(ClearCredentialStateRequest.TYPE_CLEAR_CREDENTIAL_STATE))
        }
    }

    private fun generateNonce(): String {
        val raw = ByteArray(NONCE_BYTES).also { SecureRandom().nextBytes(it) }
        return MessageDigest.getInstance("SHA-256").digest(raw)
            .joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val NONCE_BYTES = 32
    }
}
