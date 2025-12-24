package com.app.brfaceresponder.ui.screens

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.app.brfaceresponder.R
import com.app.brfaceresponder.data.SharedPrefs

data class AppInfo(
    val packageName: String,
    val name: String,
    val icon: android.graphics.drawable.Drawable? = null
)

val messagingApps = listOf(
    AppInfo("com.facebook.orca", "Facebook Messenger"),
    AppInfo("com.whatsapp", "WhatsApp"),
    AppInfo("com.whatsapp.w4b", "WhatsApp Business"),
    AppInfo("com.instagram.android", "Instagram")
    // Add other messaging apps here
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val packageManager = context.packageManager

    // State to hold the enabled apps from SharedPrefs
    val enabledAppsState = remember { mutableStateOf(SharedPrefs.getEnabledAppPackages(context)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.select_apps)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messagingApps) { appInfo ->
                AppItem(
                    appInfo = appInfo.copy(icon = getAppIcon(context, appInfo.packageName, packageManager)), // Get icon dynamically
                    initialChecked = enabledAppsState.value.contains(appInfo.packageName),
                    onCheckedChange = { isChecked ->
                        SharedPrefs.setAppEnabled(context, appInfo.packageName, isChecked)
                        // Update local state to reflect change
                        enabledAppsState.value = SharedPrefs.getEnabledAppPackages(context)
                    }
                )
            }
        }
    }
}

@Composable
fun AppItem(appInfo: AppInfo, initialChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    var isChecked by remember { mutableStateOf(initialChecked) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            appInfo.icon?.let {
                Image(
                    bitmap = it.toBitmap().asImageBitmap(),
                    contentDescription = appInfo.name,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(appInfo.name, style = MaterialTheme.typography.titleMedium)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = {
                isChecked = it
                onCheckedChange(it)
            }
        )
    }
}

@Composable
fun getAppIcon(context: Context, packageName: String, packageManager: PackageManager): android.graphics.drawable.Drawable? {
    return remember(packageName) {
        try {
            packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }
}
