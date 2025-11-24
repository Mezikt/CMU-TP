package pt.ipp.estg.cmu.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

class ThemeRepository(private val context: Context) {
    val isDarkFlow: Flow<Boolean> = ThemePreferences.isDarkThemeFlow(context, defaultValue = false)

    suspend fun setDark(enabled: Boolean) {
        ThemePreferences.setDarkTheme(context, enabled)
    }
}

