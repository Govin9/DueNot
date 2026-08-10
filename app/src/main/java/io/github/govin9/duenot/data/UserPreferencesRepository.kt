package io.github.govin9.duenot.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>) {

    // Keys
    private val THEME_MODE = stringPreferencesKey("theme_mode") // "system", "light", "dark"
    private val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol") // "₹", "$", "€", etc.
    private val REMINDER_DAYS_BEFORE = stringPreferencesKey("reminder_days_before") // "1", "2", "3", "0" (on the day)
    private val REMINDER_TIME = stringPreferencesKey("reminder_time") // "09:00", "18:00"
    private val DATE_FORMAT = stringPreferencesKey("date_format") // "dd/MM/yyyy" or "MM/dd/yyyy"

    val themeModeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "system"
    }

    val currencySymbolFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[CURRENCY_SYMBOL] ?: "₹"
    }

    val reminderDaysBeforeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[REMINDER_DAYS_BEFORE] ?: "3" // Default to 3 days before
    }

    val reminderTimeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[REMINDER_TIME] ?: "09:00" // Default to 9 AM
    }

    val dateFormatFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[DATE_FORMAT] ?: "dd/MM/yyyy"
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setCurrencySymbol(symbol: String) {
        dataStore.edit { preferences ->
            preferences[CURRENCY_SYMBOL] = symbol
        }
    }

    suspend fun setReminderDaysBefore(days: String) {
        dataStore.edit { preferences ->
            preferences[REMINDER_DAYS_BEFORE] = days
        }
    }

    suspend fun setReminderTime(time: String) {
        dataStore.edit { preferences ->
            preferences[REMINDER_TIME] = time
        }
    }

    suspend fun setDateFormat(format: String) {
        dataStore.edit { preferences ->
            preferences[DATE_FORMAT] = format
        }
    }
}
