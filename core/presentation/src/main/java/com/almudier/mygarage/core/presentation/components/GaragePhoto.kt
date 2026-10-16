package com.almudier.mygarage.core.presentation.components

import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private const val DATA_URI_MARKER = ";base64,"

/** An image model for Coil: decodes stored `data:` URIs to bytes, passes other URIs through. */
@Composable
fun rememberPhotoModel(photo: String?): Any? = remember(photo) {
    if (photo != null && photo.startsWith("data:")) {
        val start = photo.indexOf(DATA_URI_MARKER)
        if (start >= 0) {
            runCatching { Base64.decode(photo.substring(start + DATA_URI_MARKER.length), Base64.DEFAULT) }.getOrNull()
        } else null
    } else photo
}
