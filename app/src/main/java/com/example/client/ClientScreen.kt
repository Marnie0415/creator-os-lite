package com.example.client

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.TimeFormatter
import com.example.ui.theme.MoneyGreen
import com.example.ui.theme.WarningRed

@Composable
fun ClientScreen(
    viewModel: ClientViewModel,
    modifier: Modifier = Modifier,
    triggerShowAddClientDialog: Boolean = false,
    onAddClientDialogDismissed: () -> Unit = {}
) {
    val clientsList by viewModel.clientsList.collectAsState()
    val selectedClientId by viewModel.selectedClientId.collectAsState()
    val clientDetail by viewModel.clientDetail.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(triggerShowAddClientDialog) {
        if (triggerShowAddClientDialog) {
            showAddDialog = true
            onAddClientDialogDismissed()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = selectedClientId,
            transitionSpec = {
                if (targetState != null) {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                } else {
                    slideInHorizontally { width -> -width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> width } + fadeOut()
                }
            },
            label = "ClientDetailNavigation"
        ) { targetClientId ->
            if (targetClientId == null) {
                ClientListContent(
                    clients = clientsList,
                    onClientClick = { viewModel.selectClient(it.id) },
                    onAddClientClick = { showAddDialog = true }
                )
            } else {
                if (clientDetail != null) {
                    ClientDetailContent(
                        state = clientDetail!!,
                        onBack = { viewModel.selectClient(null) },
                        onDeleteClient = { viewModel.deleteClient(targetClientId) },
                        onAddTimelineEntry = { content -> viewModel.addTimelineEntry(targetClientId, content) },
                        onDeleteTimelineEntry = { entryId -> viewModel.deleteTimelineEntry(entryId) }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MoneyGreen)
                    }
                }
            }
        }

        if (showAddDialog) {
            AddClientDialog(
                onDismiss = { showAddDialog = false },
                onSave = { name, channel ->
                    viewModel.createClient(name, channel)
                    showAddDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientListContent(
    clients: List<ClientListItem>,
    onClientClick: (Client) -> Unit,
    onAddClientClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.clients_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = {
                    IconButton(
                        onClick = onAddClientClick,
                        modifier = Modifier.testTag("add_client_icon_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.cd_clients_add_client), tint = MoneyGreen)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (clients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.testTag("empty_clients_view")
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = stringResource(R.string.clients_empty_title),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.clients_empty_title),
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.clients_empty_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onAddClientClick,
                        colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("empty_add_client_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.clients_add), fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(clients, key = { it.client.id }) { item ->
                    ClientRowCard(item = item, onClick = { onClientClick(item.client) })
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun ClientRowCard(item: ClientListItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("client_card_${item.client.id}")
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.client.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .background(MoneyGreen.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.client.contactChannel.uppercase(),
                        color = MoneyGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.clients_last_contact),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = TimeFormatter.getRelativeTime(item.client.lastContactTimestamp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.clients_active_projects),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "${item.activeProjectsCount}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.clients_outstanding),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = com.example.ui.CurrencyUtils.format(item.outstandingRevenue),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (item.outstandingRevenue > 0) WarningRed else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailContent(
    state: ClientDetailState,
    onBack: () -> Unit,
    onDeleteClient: () -> Unit,
    onAddTimelineEntry: (String) -> Unit,
    onDeleteTimelineEntry: (String) -> Unit
) {
    var newEntryText by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.client?.name ?: stringResource(R.string.clients_detail_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_clients_go_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.cd_clients_delete_client), tint = WarningRed)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.clients_metrics),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(stringResource(R.string.clients_contact_type), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(state.client?.contactChannel ?: "Other", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(stringResource(R.string.clients_balance), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(
                                    text = com.example.ui.CurrencyUtils.format(state.outstandingRevenue),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.outstandingRevenue > 0) WarningRed else MoneyGreen
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.clients_log_title),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = newEntryText,
                            onValueChange = { newEntryText = it },
                            placeholder = { Text(stringResource(R.string.clients_log_placeholder), fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth().testTag("timeline_input_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MoneyGreen,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (newEntryText.isNotBlank()) {
                                    onAddTimelineEntry(newEntryText.trim())
                                    newEntryText = ""
                                }
                            },
                            enabled = newEntryText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MoneyGreen,
                                contentColor = Color.Black,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.align(Alignment.End).testTag("save_timeline_entry_button")
                        ) {
                            Text(stringResource(R.string.clients_log_button), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.clients_timeline),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (state.timeline.isEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.clients_timeline_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(state.timeline, key = { it.id }) { entry ->
                    TimelineEntryRow(entry = entry, onDelete = { onDeleteTimelineEntry(entry.id) })
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.clients_delete_title)) },
            text = { Text(stringResource(R.string.clients_delete_message, state.client?.name ?: "")) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClient()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningRed)
                ) {
                    Text(stringResource(R.string.clients_delete_confirm), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.clients_delete_cancel), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

@Composable
fun TimelineEntryRow(entry: TimelineEntry, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("timeline_row_${entry.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = TimeFormatter.getRelativeTime(entry.timestamp),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = MoneyGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = entry.content, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_clients_save_entry),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun AddClientDialog(onDismiss: () -> Unit, onSave: (String, ContactChannel) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedChannel by remember { mutableStateOf(ContactChannel.Discord) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.clients_create_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.clients_create_name_label)) },
                    modifier = Modifier.fillMaxWidth().testTag("client_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MoneyGreen,
                        focusedLabelColor = MoneyGreen
                    ),
                    singleLine = true
                )

                Column {
                    Text(
                        stringResource(R.string.clients_create_channel_label),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ContactChannel.values().forEach { channel ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { selectedChannel = channel }.padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = (selectedChannel == channel),
                                onClick = { selectedChannel = channel },
                                colors = RadioButtonDefaults.colors(selectedColor = MoneyGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(channel.name, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name.trim(), selectedChannel)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_client_button")
            ) {
                Text(stringResource(R.string.clients_create_button), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("dismiss_client_button")) {
                Text(stringResource(R.string.clients_create_cancel), color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
