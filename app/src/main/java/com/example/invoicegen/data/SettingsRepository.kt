package com.example.invoicegen.data

import kotlinx.coroutines.flow.Flow

// This class is actually SettingsRepositoryImpl and inherit from SettingsRepository
class SettingsRepository(private val settingsDao: SettingsDao) {
    // This should be in the view model. Just have a method the view model calls here to access
    // settingDao.getSettings. Generally I try to avoid flows in repositories like this.
    val companySettings: Flow<CompanySettings?> = settingsDao.getSettings()

    suspend fun updateSettings(settings: CompanySettings) {
        settingsDao.updateSettings(settings)
    }
}

// In your domain layer, you should have

//interface SettingsRepository {
//    val companySettings: Flow<CompanySettings?>
//
//    suspend fun updateSettings(settings: CompanySettings)
//}

// This is the type you would give your injection sites. They need not be aware of the implementation.