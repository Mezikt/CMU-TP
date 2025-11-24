package pt.ipp.estg.cmu.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATASTORE_NAME = "settings_datastore"
private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

object ThemePreferences {
    private val DARK_THEME_KEY = booleanPreferencesKey("dark_theme")

    fun isDarkThemeFlow(context: Context, defaultValue: Boolean = false): Flow<Boolean> {
        return context.dataStore.data
            .map { prefs -> prefs[DARK_THEME_KEY] ?: defaultValue }
    }

    suspend fun setDarkTheme(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_THEME_KEY] = enabled
        }
    }
}
