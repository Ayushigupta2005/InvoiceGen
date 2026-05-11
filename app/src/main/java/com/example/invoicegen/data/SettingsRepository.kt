package com.example.invoicegen.data

import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val settingsDao: SettingsDao) {
    val companySettings: Flow<CompanySettings?> = settingsDao.getSettings()

    suspend fun updateSettings(settings: CompanySettings) {
        settingsDao.updateSettings(settings)
    }
}
