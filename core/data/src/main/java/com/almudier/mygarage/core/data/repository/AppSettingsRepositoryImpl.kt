package com.almudier.mygarage.core.data.repository

import com.almudier.mygarage.core.data.datastore.UserPreferencesDataSource
import com.almudier.mygarage.core.domain.model.DefaultServiceIntervals
import com.almudier.mygarage.core.domain.model.ServiceInterval
import com.almudier.mygarage.core.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsRepositoryImpl @Inject constructor(
    private val preferences: UserPreferencesDataSource,
) : AppSettingsRepository {

    override val selectedVehicleId: Flow<String?> =
        preferences.userPreferences.map { it.selectedVehicleId }.distinctUntilChanged()

    override val currency: Flow<String> =
        preferences.userPreferences.map { it.currency }.distinctUntilChanged()

    override val serviceIntervals: Flow<List<ServiceInterval>> =
        preferences.userPreferences
            .map { prefs -> DefaultServiceIntervals.map { it.copy(months = prefs.intervalMonths[it.id] ?: it.months) } }
            .distinctUntilChanged()

    override suspend fun selectVehicle(id: String?) {
        preferences.update { it.copy(selectedVehicleId = id) }
    }

    override suspend fun setCurrency(code: String) {
        preferences.update { it.copy(currency = code) }
    }

    override suspend fun setIntervalMonths(id: String, months: Int) {
        preferences.update { it.copy(intervalMonths = it.intervalMonths + (id to months.coerceIn(1, 60))) }
    }
}
