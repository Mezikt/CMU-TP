package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FriendsPage(
    onNavigateBack: () -> Unit,
) {
    // Box para permitir sobrepor elementos
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Botão de voltar, alinhado no canto superior esquerdo
        ButtonBack(
            onClick = onNavigateBack,
            modifier = Modifier.align(Alignment.TopStart)
        )

        // Texto, alinhado ao centro do Box
        Text(
            text = "Página de Amigos",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}


@Composable
private fun ButtonBack(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.padding(8.dp) // Adiciona um padding para não colar na borda
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Voltar"
        )
    }
}
