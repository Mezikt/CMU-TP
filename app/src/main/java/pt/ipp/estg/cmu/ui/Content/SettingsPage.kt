package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.ipp.estg.cmu.R
import pt.ipp.estg.cmu.ui.theme.ThemeViewModel

@Composable
fun SettingsPage(
    toChangePassword: () -> Unit,
    themeViewModel: ThemeViewModel = viewModel()
) {
    val isDarkTheme by themeViewModel.isDark.collectAsState()
    var notificationsEnabled by remember { mutableStateOf(true) }
    var locationServicesEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = stringResource(R.string.header_account_settings),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        SettingItem(
            title = stringResource(R.string.pref_change_password),
            onClick = toChangePassword
        )
        SettingItem(
            title = stringResource(R.string.pref_update_email),
            onClick = { /* TODO: Handle click */ }
        )
        SettingItem(
            title = stringResource(R.string.pref_manage_profile),
            onClick = { /* TODO: Handle click */ }
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))


        Text(
            text = stringResource(R.string.header_app_preferences),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        SwitchSettingItem(
            title = stringResource(R.string.pref_dark_mode),
            checked = isDarkTheme,
            onCheckedChange = { themeViewModel.setDark(it) }
        )

        SwitchSettingItem(
            title = stringResource(R.string.pref_notifications),
            checked = notificationsEnabled,
            onCheckedChange = { notificationsEnabled = it }
        )
        SwitchSettingItem(
            title = stringResource(R.string.pref_location),
            checked = locationServicesEnabled,
            onCheckedChange = { locationServicesEnabled = it }
        )
    }
}

@Composable
private fun SettingItem(title: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun SwitchSettingItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}