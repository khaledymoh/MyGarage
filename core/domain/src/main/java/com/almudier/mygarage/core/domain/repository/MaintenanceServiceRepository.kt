package com.almudier.mygarage.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface MaintenanceServiceRepository {
    fun observeServices(): Flow<List<String>>
    suspend fun addServices(names: List<String>)
}
