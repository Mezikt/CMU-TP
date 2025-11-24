package pt.ipp.estg.cmu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme // Import necessário
import androidx.compose.material3.Surface     // Import necessário
import androidx.compose.ui.Modifier
import pt.ipp.estg.cmu.ui.theme.CMU_TPTheme
import pt.ipp.estg.cmu.ui.theme.ThemeViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

class MainActivity : ComponentActivity() {
    private val themeViewModel: ThemeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark by themeViewModel.isDark.collectAsState()

            CMU_TPTheme(darkTheme = isDark) {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    MainNavHost()
                }
            }
        }
    }
}