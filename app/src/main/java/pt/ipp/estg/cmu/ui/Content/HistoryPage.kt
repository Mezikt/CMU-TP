package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HistoryPage(
    onNavigateBack: () -> Unit,
    ){
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        ButtonBack(
            onClick = onNavigateBack,
            modifier = Modifier.align(Alignment.TopStart)
        )

        Text(
            text = "Página de Histórico",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}


@Composable
private fun ButtonBack(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){
    IconButton(
        onClick = onClick,
        modifier = modifier.padding(8.dp)
    ){
        Icon(
             imageVector = Icons.Default.ArrowBack,
             contentDescription = "Voltar",
            )
    }
}