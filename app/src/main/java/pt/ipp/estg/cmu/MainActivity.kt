package pt.ipp.estg.cmu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import pt.ipp.estg.cmu.ui.theme.CMU_TPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CMU_TPTheme {
                MainNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
