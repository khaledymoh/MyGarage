package com.almudier.mygarage.authentication.data.mapper

import com.almudier.mygarage.authentication.data.model.UserDto
import com.almudier.mygarage.authentication.domain.model.AuthUser
import com.almudier.mygarage.core.data.model.UserPreferences
import com.google.firebase.auth.FirebaseUser

fun FirebaseUser.toDto(): UserDto = UserDto(
    uid = uid,
    email = email,
    displayName = displayName,
    photoUrl = photoUrl?.toString(),
)

fun UserDto.toDomain(): AuthUser = AuthUser(
    uid = uid,
    email = email,
    displayName = displayName,
    photoUrl = photoUrl,
)

fun UserPreferences.toDomainOrNull(): AuthUser? {
    val userId = uid
    return if (isSignedIn && userId != null) {
        AuthUser(uid = userId, email = email, displayName = displayName, photoUrl = photoUrl)
    } else {
        null
    }
}
