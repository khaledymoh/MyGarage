package com.almudier.mygarage.core.data.network

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.channels.ProducerScope

/**
 * Ends a snapshot-listener flow after a Firestore error. Signing out revokes access before the
 * listeners are cancelled, so their "permission denied" is expected then: end quietly instead
 * of surfacing an error. Any other error closes the flow with a [NetworkException].
 */
internal fun ProducerScope<*>.closeWithListenerError(error: FirebaseFirestoreException) {
    val signedOut = FirebaseAuth.getInstance().currentUser == null
    if (signedOut && error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
        close()
    } else {
        close(error.toNetworkException())
    }
}
