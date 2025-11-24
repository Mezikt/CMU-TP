package pt.ipp.estg.cmu.ui.Content

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.ipp.estg.cmu.ui.theme.CMU_TPTheme

@Composable
fun SettingsPage() {
    var isDarkTheme by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var locationServicesEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Account Settings Section
        Text(
            text = "Account Settings",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        SettingItem(title = "Change Password", onClick = { /* TODO: Handle click */ })
        SettingItem(title = "Update Email Address", onClick = { /* TODO: Handle click */ })
        SettingItem(title = "Manage Profile Information", onClick = { /* TODO: Handle click */ })

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // App Preferences Section
        Text(
            text = "App Preferences",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        SwitchSettingItem(
            title = "Theme (Dark Mode)",
            checked = isDarkTheme,
            onCheckedChange = { isDarkTheme = it }
        )
        // You can add logic here to handle "System Default"
        SwitchSettingItem(
            title = "Notifications",
            checked = notificationsEnabled,
            onCheckedChange = { notificationsEnabled = it }
        )
        SwitchSettingItem(
            title = "Location Services",
            checked = locationServicesEnabled,
            onCheckedChange = { locationServicesEnabled = it }
        )
    }
}

@Composable
private fun SettingItem(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
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

@Preview(showBackground = true)
@Composable
fun SettingsPagePreview() {
    CMU_TPTheme {
        SettingsPage()
    }
}