package io.github.govin9.duenot

import android.app.Application
import io.github.govin9.duenot.data.AppDatabase
import io.github.govin9.duenot.data.AppRepository

import io.github.govin9.duenot.data.UserPreferencesRepository
import io.github.govin9.duenot.data.dataStore

class MainApplication : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { AppRepository(database.cardDao(), database.paymentDao()) }
    val userPreferencesRepository by lazy { UserPreferencesRepository(dataStore) }
}
