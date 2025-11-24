package pt.ipp.estg.cmu.ui.theme

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.ipp.estg.cmu.data.ThemeRepository

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ThemeRepository(application.applicationContext)

    private val _isDark = MutableStateFlow(false)
    val isDark: StateFlow<Boolean> = _isDark

    init {
        viewModelScope.launch {
            repository.isDarkFlow.collect { value ->
                _isDark.value = value
            }
        }
    }

    fun setDark(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDark(enabled)
        }
    }
}
