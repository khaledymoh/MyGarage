package com.almudier.mygarage.update

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** A newer release to offer. [required] blocks the app until the user updates. */
data class AppUpdate(val versionName: String, val url: String, val required: Boolean)

/**
 * Compares the installed version with the one the release workflow publishes to Firebase Remote
 * Config (`latest_version_code`, `min_version_code`, `latest_version_name`, `update_url`).
 * Listens in real time, so a new release reaches open apps within seconds.
 */
@Singleton
class AppUpdateChecker @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val remoteConfig = FirebaseRemoteConfig.getInstance()

    private val installedVersionCode: Long by lazy {
        PackageInfoCompat.getLongVersionCode(context.packageManager.getPackageInfo(context.packageName, 0))
    }

    /** The update to offer, or null when this install is up to date. */
    val update: Flow<AppUpdate?> = callbackFlow {
        remoteConfig.setConfigSettingsAsync(
            FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(FETCH_INTERVAL_SECONDS).build(),
        )
        remoteConfig.setDefaultsAsync(
            mapOf(KEY_LATEST_CODE to 0L, KEY_MIN_CODE to 0L, KEY_LATEST_NAME to "", KEY_URL to ""),
        ).await()
        trySend(current())
        // Offline or failed fetches keep the last activated values (or the defaults: no update).
        runCatching { remoteConfig.fetchAndActivate().await() }
        trySend(current())

        val registration = remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                remoteConfig.activate().addOnCompleteListener { trySend(current()) }
            }

            override fun onError(error: FirebaseRemoteConfigException) = Unit
        })
        awaitClose { registration.remove() }
    }.distinctUntilChanged()

    private fun current(): AppUpdate? {
        val latest = remoteConfig.getLong(KEY_LATEST_CODE)
        val minimum = remoteConfig.getLong(KEY_MIN_CODE)
        val url = remoteConfig.getString(KEY_URL)
        val required = minimum > installedVersionCode
        if (url.isBlank() || (!required && latest <= installedVersionCode)) return null
        return AppUpdate(versionName = remoteConfig.getString(KEY_LATEST_NAME), url = url, required = required)
    }

    private companion object {
        const val KEY_LATEST_CODE = "latest_version_code"
        const val KEY_MIN_CODE = "min_version_code"
        const val KEY_LATEST_NAME = "latest_version_name"
        const val KEY_URL = "update_url"
        const val FETCH_INTERVAL_SECONDS = 3600L
    }
}
