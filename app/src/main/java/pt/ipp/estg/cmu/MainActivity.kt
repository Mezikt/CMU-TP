package pt.ipp.estg.cmu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme // Import necessário
import androidx.compose.material3.Surface     // Import necessário
import androidx.compose.ui.Modifier
import pt.ipp.estg.cmu.ui.theme.CMU_TPTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CMU_TPTheme {
                // CORREÇÃO: Envolve a tua aplicação numa Surface para lhe dar uma cor de fundo.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background // Usa a cor de fundo do teu tema
                ) {
                    // O MainNavHost agora é desenhado sobre a Surface.
                    MainNavHost()
                }
            }
        }
    }
}