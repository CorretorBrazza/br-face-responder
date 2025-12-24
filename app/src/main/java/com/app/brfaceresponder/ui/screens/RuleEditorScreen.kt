package com.app.brfaceresponder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.app.brfaceresponder.R
import com.app.brfaceresponder.ui.viewmodel.RuleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleEditorScreen(
    viewModel: RuleViewModel,
    onNavigateBack: () -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    var replyMessage by remember { mutableStateOf("") }
    var isRegex by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf("0") }
    var delaySeconds by remember { mutableStateOf("0") } // New state for delaySeconds
    val error by viewModel.error.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.add_new_rule)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val parsedPriority = priority.toIntOrNull() ?: 0
                val parsedDelaySeconds = delaySeconds.toIntOrNull() ?: 0 // Default to 0 if invalid
                if (keyword.isNotBlank() && replyMessage.isNotBlank()) {
                    viewModel.addRule(keyword, replyMessage, isRegex, parsedPriority, parsedDelaySeconds) {
                        onNavigateBack()
                    }
                }
            }) {
                Text(stringResource(id = R.string.save), modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = keyword,
                onValueChange = { keyword = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.keyword_label)) },
                placeholder = { Text(stringResource(id = R.string.keyword_placeholder)) },
                isError = error != null
            )
            OutlinedTextField(
                value = replyMessage,
                onValueChange = { replyMessage = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.reply_label)) },
                placeholder = { Text(stringResource(id = R.string.reply_placeholder)) }
            )
            OutlinedTextField(
                value = priority,
                onValueChange = { newValue ->
                    // Only allow numeric input
                    if (newValue.all { it.isDigit() }) {
                        priority = newValue
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.priority_label)) },
                placeholder = { Text(stringResource(id = R.string.priority_placeholder)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = delaySeconds,
                onValueChange = { newValue ->
                    if (newValue.all { it.isDigit() }) {
                        delaySeconds = newValue
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(id = R.string.delay_label)) },
                placeholder = { Text(stringResource(id = R.string.delay_placeholder)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isRegex,
                    onCheckedChange = { isRegex = it }
                )
                Text(stringResource(id = R.string.use_regex))
            }
            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}