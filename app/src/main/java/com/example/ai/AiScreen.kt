package com.example.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.MoneyGreen
import com.example.ui.theme.WarningRed

@Composable
fun AiScreen(
    viewModel: AiViewModel,
    ghostedOrOverdueClients: List<Pair<String, Pair<Double, String>>>,
    modifier: Modifier = Modifier
) {
    val followUpState by viewModel.followUpState.collectAsState()
    val quoteState by viewModel.quoteState.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    var activeTab by remember { mutableStateOf(0) }
    var showCopiedToast by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.ai_header),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MoneyGreen,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = stringResource(R.string.ai_title),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MoneyGreen
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text(stringResource(R.string.ai_tab_followup), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text(stringResource(R.string.ai_tab_quote), fontWeight = FontWeight.Bold) }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (activeTab == 0) {
                    item {
                        Text(
                            text = stringResource(R.string.ai_followup_title),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    item {
                        var clientName by remember { mutableStateOf("") }
                        var amountStr by remember { mutableStateOf("") }
                        var selectedTone by remember { mutableStateOf("Friendly") }
                        var situationText by remember { mutableStateOf("Payment limit expired by 3 days") }

                        var expandedTone by remember { mutableStateOf(false) }
                        var expandedClientSelector by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (ghostedOrOverdueClients.isNotEmpty()) {
                                    Column {
                                        Text(
                                            stringResource(R.string.ai_followup_autofill_label),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = MoneyGreen
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            OutlinedButton(
                                                onClick = { expandedClientSelector = true },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    stringResource(R.string.ai_followup_autofill_button),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = expandedClientSelector,
                                                onDismissRequest = { expandedClientSelector = false }
                                            ) {
                                                ghostedOrOverdueClients.forEach { (name, data) ->
                                                    val (amt, sit) = data
                                                    DropdownMenuItem(
                                                        text = { Text("$name ($sit)") },
                                                        onClick = {
                                                            clientName = name
                                                            amountStr = amt.toString()
                                                            situationText = sit
                                                            expandedClientSelector = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = clientName,
                                    onValueChange = { clientName = it },
                                    label = { Text(stringResource(R.string.ai_followup_client_label)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MoneyGreen,
                                        focusedLabelColor = MoneyGreen
                                    ),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = amountStr,
                                    onValueChange = { amountStr = it },
                                    label = { Text(stringResource(R.string.ai_followup_amount_label)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MoneyGreen,
                                        focusedLabelColor = MoneyGreen
                                    ),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = situationText,
                                    onValueChange = { situationText = it },
                                    label = { Text(stringResource(R.string.ai_followup_situation_label)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MoneyGreen,
                                        focusedLabelColor = MoneyGreen
                                    ),
                                    singleLine = true
                                )

                                Column {
                                    Text(
                                        stringResource(R.string.ai_followup_tone_label),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedButton(
                                            onClick = { expandedTone = true },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(selectedTone, color = MaterialTheme.colorScheme.onSurface)
                                        }
                                        DropdownMenu(
                                            expanded = expandedTone,
                                            onDismissRequest = { expandedTone = false }
                                        ) {
                                            listOf(
                                                stringResource(R.string.ai_followup_tone_friendly),
                                                stringResource(R.string.ai_followup_tone_professional),
                                                stringResource(R.string.ai_followup_tone_firm)
                                            ).forEach { tone ->
                                                DropdownMenuItem(
                                                    text = { Text(tone) },
                                                    onClick = {
                                                        selectedTone = tone
                                                        expandedTone = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        if (clientName.isNotBlank()) {
                                            viewModel.generateFollowUp(
                                                clientName = clientName,
                                                amount = amountStr.toDoubleOrNull() ?: 0.0,
                                                contextInfo = situationText,
                                                tone = selectedTone
                                            )
                                        }
                                    },
                                    enabled = clientName.isNotBlank() && followUpState != AiState.Loading,
                                    colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen, contentColor = Color.Black),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.ai_followup_button), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        when (val state = followUpState) {
                            is AiState.Loading -> {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MoneyGreen)
                                }
                            }
                            is AiState.Success -> {
                                ResultCard(
                                    title = stringResource(R.string.ai_followup_title),
                                    resultText = state.result,
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(state.result))
                                        showCopiedToast = true
                                    },
                                    onClear = { viewModel.clearFollowUp() }
                                )
                            }
                            is AiState.Error -> {
                                ErrorCard(message = state.message)
                            }
                            else -> {}
                        }
                    }
                } else {
                    item {
                        Text(
                            text = stringResource(R.string.ai_quote_title),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    item {
                        var requirements by remember { mutableStateOf("") }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = requirements,
                                    onValueChange = { requirements = it },
                                    placeholder = { Text(stringResource(R.string.ai_quote_placeholder)) },
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MoneyGreen,
                                        focusedLabelColor = MoneyGreen
                                    ),
                                    maxLines = 6
                                )

                                Button(
                                    onClick = {
                                        if (requirements.isNotBlank()) {
                                            viewModel.generateQuote(requirements)
                                        }
                                    },
                                    enabled = requirements.isNotBlank() && quoteState != AiState.Loading,
                                    colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen, contentColor = Color.Black),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.ai_quote_button), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        when (val state = quoteState) {
                            is AiState.Loading -> {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MoneyGreen)
                                }
                            }
                            is AiState.Success -> {
                                ResultCard(
                                    title = stringResource(R.string.ai_quote_title),
                                    resultText = state.result,
                                    onCopy = {
                                        clipboardManager.setText(AnnotatedString(state.result))
                                        showCopiedToast = true
                                    },
                                    onClear = { viewModel.clearQuote() }
                                )
                            }
                            is AiState.Error -> {
                                ErrorCard(message = state.message)
                            }
                            else -> {}
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        if (showCopiedToast) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                showCopiedToast = false
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    border = BorderStroke(1.dp, MoneyGreen),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Text(
                        stringResource(R.string.ai_copied_toast),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontFamily = FontFamily.Monospace,
                        color = MoneyGreen,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ResultCard(
    title: String,
    resultText: String,
    onCopy: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MoneyGreen.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = MoneyGreen
                )
                Text(
                    text = stringResource(R.string.ai_result_clear),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.clickable(onClick = onClear)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = resultText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onCopy,
                colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.ai_result_copy), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, WarningRed)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = WarningRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.ai_error_title),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = WarningRed
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        }
    }
}
