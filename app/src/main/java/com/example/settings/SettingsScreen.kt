package com.example.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ai.AiProviderType
import com.example.ai.AiServiceManager
import com.example.ai.AnthropicService
import com.example.ai.GeminiApiClient
import com.example.ai.OpenAiService
import com.example.ui.SettingsManager
import com.example.ui.theme.MoneyGreen
import com.example.ui.theme.WarningRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onResetDatabase: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentProviderType by settingsManager.providerType.collectAsState()
    val currentProviderApiKey by settingsManager.providerApiKey.collectAsState()
    val currentProviderModel by settingsManager.providerModel.collectAsState()
    val currentProviderBaseUrl by settingsManager.providerBaseUrl.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }

    var selectedProviderType by remember(currentProviderType) { mutableStateOf(currentProviderType) }
    var providerApiKeyInput by remember(currentProviderApiKey) { mutableStateOf(currentProviderApiKey) }
    var providerModelInput by remember(currentProviderModel) { mutableStateOf(currentProviderModel) }
    var providerBaseUrlInput by remember(currentProviderBaseUrl) { mutableStateOf(currentProviderBaseUrl) }
    var showProviderKey by remember { mutableStateOf(false) }
    var keySavedNotice by remember { mutableStateOf(false) }

    var showProviderDropdown by remember { mutableStateOf(false) }
    var showModelDropdown by remember { mutableStateOf(false) }
    var showProviderBaseUrlDropdown by remember { mutableStateOf(false) }

    val modelSuggestions = when (selectedProviderType) {
        AiProviderType.OPENAI_COMPATIBLE -> OpenAiService.POPULAR_MODELS
        AiProviderType.ANTHROPIC -> AnthropicService.POPULAR_MODELS
        AiProviderType.GEMINI -> linkedMapOf(
            "Gemini 2.5 Flash" to "gemini-2.5-flash",
            "Gemini 2.5 Pro" to "gemini-2.5-pro",
            "Gemini 2.0 Flash" to "gemini-2.0-flash"
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                Text(
                    text = stringResource(R.string.settings_header_prefix),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MoneyGreen,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // ---- Provider Selection Card ----
            Card(
                modifier = Modifier.fillMaxWidth().testTag("ai_provider_card"),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MoneyGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.settings_provider_card_title),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MoneyGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.settings_provider_card_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Provider Type Dropdown
                    Text(
                        stringResource(R.string.settings_provider_label),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showProviderDropdown = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "${selectedProviderType.displayName}  •  ${selectedProviderType.description}",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp
                            )
                        }
                        DropdownMenu(
                            expanded = showProviderDropdown,
                            onDismissRequest = { showProviderDropdown = false }
                        ) {
                            AiProviderType.values().forEach { provider ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(provider.displayName, fontWeight = FontWeight.Bold)
                                            Text(
                                                provider.description,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedProviderType = provider
                                        providerModelInput = ""
                                        providerBaseUrlInput = ""
                                        showProviderDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // API Key Input
                    OutlinedTextField(
                        value = providerApiKeyInput,
                        onValueChange = { providerApiKeyInput = it },
                        label = { Text(stringResource(R.string.settings_api_key_label)) },
                        placeholder = { Text(stringResource(R.string.settings_api_key_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MoneyGreen,
                            focusedLabelColor = MoneyGreen
                        ),
                        visualTransformation = if (showProviderKey) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { showProviderKey = !showProviderKey }) {
                                Icon(
                                    imageVector = if (showProviderKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showProviderKey) stringResource(R.string.cd_settings_hide_key) else stringResource(R.string.cd_settings_show_key)
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Model Selector
                    Text(
                        stringResource(R.string.settings_model_label),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = providerModelInput,
                        onValueChange = { providerModelInput = it },
                        label = { Text(stringResource(R.string.settings_model_label)) },
                        placeholder = { Text(selectedProviderType.getDefaultModel()) },
                        modifier = Modifier.fillMaxWidth().testTag("model_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MoneyGreen,
                            focusedLabelColor = MoneyGreen
                        ),
                        singleLine = true,
                        trailingIcon = {
                            Box {
                                IconButton(onClick = { showModelDropdown = !showModelDropdown }) {
                                    Text("▼", fontSize = 10.sp)
                                }
                                DropdownMenu(
                                    expanded = showModelDropdown,
                                    onDismissRequest = { showModelDropdown = false }
                                ) {
                                    modelSuggestions.forEach { (label, modelId) ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(label, fontWeight = FontWeight.Medium)
                                                    Text(
                                                        modelId,
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                            },
                                            onClick = {
                                                providerModelInput = modelId
                                                showModelDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    )

                    // Base URL (only for OpenAI-compatible)
                    if (selectedProviderType == AiProviderType.OPENAI_COMPATIBLE) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            stringResource(R.string.settings_base_url_label),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = providerBaseUrlInput,
                            onValueChange = { providerBaseUrlInput = it },
                            label = { Text(stringResource(R.string.settings_base_url_label)) },
                            placeholder = { Text(stringResource(R.string.settings_base_url_placeholder)) },
                            modifier = Modifier.fillMaxWidth().testTag("base_url_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MoneyGreen,
                                focusedLabelColor = MoneyGreen
                            ),
                            singleLine = true,
                            trailingIcon = {
                                Box {
                                    IconButton(onClick = { showProviderBaseUrlDropdown = !showProviderBaseUrlDropdown }) {
                                        Text("▼", fontSize = 10.sp)
                                    }
                                    DropdownMenu(
                                        expanded = showProviderBaseUrlDropdown,
                                        onDismissRequest = { showProviderBaseUrlDropdown = false }
                                    ) {
                                        OpenAiService.KNOWN_PROVIDERS.forEach { (name, url) ->
                                            DropdownMenuItem(
                                                text = {
                                                    Column {
                                                        Text(name, fontWeight = FontWeight.Medium)
                                                        Text(
                                                            url,
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.secondary,
                                                            fontFamily = FontFamily.Monospace
                                                        )
                                                    }
                                                },
                                                onClick = {
                                                    providerBaseUrlInput = url
                                                    showProviderBaseUrlDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Save Button
                    Button(
                        onClick = {
                            val config = com.example.ai.AiProviderConfig(
                                type = selectedProviderType,
                                apiKey = providerApiKeyInput.trim(),
                                model = providerModelInput.trim(),
                                baseUrl = providerBaseUrlInput.trim()
                            )
                            settingsManager.setProviderConfig(config)
                            GeminiApiClient.userApiKey = providerApiKeyInput.trim()
                            AiServiceManager.reset()
                            keySavedNotice = true
                        },
                        enabled = providerApiKeyInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MoneyGreen,
                            contentColor = Color.Black,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_provider_button")
                    ) {
                        Text(stringResource(R.string.settings_save_button), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Saved notification
            if (keySavedNotice) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(3000)
                    keySavedNotice = false
                }
                Card(
                    border = BorderStroke(1.dp, MoneyGreen),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        text = stringResource(R.string.settings_saved_notice, selectedProviderType.displayName),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontFamily = FontFamily.Monospace,
                        color = MoneyGreen,
                        fontSize = 11.sp
                    )
                }
            }

            // Provider Quick Reference (only for OpenAI-compatible)
            if (selectedProviderType == AiProviderType.OPENAI_COMPATIBLE) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.settings_openai_ref_title),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = OpenAiService.KNOWN_PROVIDERS.entries.joinToString("\n") { "• ${it.key}: ${it.value}" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // App Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MoneyGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.settings_about_title),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MoneyGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.settings_about_text),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Danger Zone
            Card(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, WarningRed.copy(alpha = 0.5f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = WarningRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.settings_danger_title),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarningRed
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.settings_danger_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        border = BorderStroke(1.dp, WarningRed),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarningRed),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reset_db_button")
                    ) {
                        Text(stringResource(R.string.settings_danger_button), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.settings_reset_title)) },
            text = { Text(stringResource(R.string.settings_reset_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDatabase()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningRed)
                ) {
                    Text(stringResource(R.string.settings_reset_confirm), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.settings_reset_cancel), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

private fun AiProviderType.getDefaultModel(): String = when (this) {
    AiProviderType.GEMINI -> "gemini-2.5-flash"
    AiProviderType.OPENAI_COMPATIBLE -> "gpt-4o-mini"
    AiProviderType.ANTHROPIC -> "claude-sonnet-4"
}
