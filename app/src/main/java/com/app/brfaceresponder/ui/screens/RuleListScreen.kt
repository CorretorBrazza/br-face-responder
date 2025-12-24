package com.app.brfaceresponder.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.app.brfaceresponder.R
import com.app.brfaceresponder.data.SharedPrefs
import com.app.brfaceresponder.model.Rule
import com.app.brfaceresponder.ui.viewmodel.RuleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleListScreen(
    viewModel: RuleViewModel,
    onAddRuleClicked: () -> Unit,
    onManageAppsClicked: () -> Unit
) {
    val rules by viewModel.rules.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar erros via Snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    var isServiceEnabled by remember {
        mutableStateOf(SharedPrefs.isServiceEnabled(context))
    }

    val hasNotificationPermission by rememberUpdatedState(
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.app_name)) },
                actions = {
                    IconButton(onClick = onManageAppsClicked) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(id = R.string.select_apps))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddRuleClicked) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.add_rule))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(id = R.string.auto_reply_service), style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = isServiceEnabled,
                            onCheckedChange = {
                                isServiceEnabled = it
                                SharedPrefs.setServiceEnabled(context, it)
                            },
                            enabled = hasNotificationPermission
                        )
                    }
                    if (!hasNotificationPermission) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            stringResource(id = R.string.permission_needed),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        }) {
                            Text(stringResource(id = R.string.grant_permission))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(stringResource(id = R.string.rules), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(rules) { rule ->
                    RuleItem(
                        rule = rule,
                        onDelete = { viewModel.deleteRule(rule) },
                        onIncreasePriority = { viewModel.increaseRulePriority(rule) },
                        onDecreasePriority = { viewModel.decreaseRulePriority(rule) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
fun RuleItem(rule: Rule, onDelete: () -> Unit, onIncreasePriority: () -> Unit, onDecreasePriority: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(id = R.string.if_message_contains, rule.keyword), style = MaterialTheme.typography.bodyLarge)
            Text(text = stringResource(id = R.string.reply_with, rule.replyMessage), style = MaterialTheme.typography.bodyMedium)
            Text(text = stringResource(id = R.string.priority_label) + ": " + rule.priority, style = MaterialTheme.typography.bodySmall)
            if (rule.isRegex) {
                Text(stringResource(id = R.string.use_regex) + ": Sim", style = MaterialTheme.typography.bodySmall)
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onIncreasePriority) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = stringResource(id = R.string.increase_priority))
            }
            Text(text = rule.priority.toString(), style = MaterialTheme.typography.bodySmall)
            IconButton(onClick = onDecreasePriority) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = stringResource(id = R.string.decrease_priority))
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(id = R.string.delete_rule), tint = MaterialTheme.colorScheme.error)
        }
    }
}