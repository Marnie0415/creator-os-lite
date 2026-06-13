package com.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Delete
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
import com.example.client.Client
import com.example.invoice.InvoiceStatus
import com.example.invoice.InvoiceViewModel
import com.example.ui.TimeFormatter
import com.example.ui.theme.MoneyGreen
import com.example.ui.theme.WarningRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectScreen(
    projectViewModel: ProjectViewModel,
    invoiceViewModel: InvoiceViewModel,
    clients: List<Client>,
    onNavigateToClients: () -> Unit,
    modifier: Modifier = Modifier,
    triggerShowAddProjectDialog: Boolean = false,
    onAddProjectDialogDismissed: () -> Unit = {}
) {
    val projectsList by projectViewModel.projectsList.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(triggerShowAddProjectDialog) {
        if (triggerShowAddProjectDialog) {
            showAddDialog = true
            onAddProjectDialogDismissed()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.projects_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = {
                    IconButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("add_project_icon_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.cd_projects_add), tint = MoneyGreen)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (projectsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.testTag("empty_projects_view")
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = stringResource(R.string.projects_empty_title),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.projects_empty_title),
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.projects_empty_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    if (clients.isEmpty()) {
                        Button(
                            onClick = onNavigateToClients,
                            colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("empty_create_client_first_button")
                        ) {
                            Text(stringResource(R.string.projects_empty_create_client_first), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("empty_add_project_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.projects_empty_add_button), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(projectsList, key = { it.project.id }) { item ->
                    ProjectRowCard(
                        item = item,
                        onUpdateProjectStatus = { newStatus ->
                            projectViewModel.updateProjectStatus(item.project.id, newStatus)
                        },
                        onUpdateInvoiceStatus = { newStatus ->
                            if (item.invoice != null) {
                                invoiceViewModel.updateInvoiceStatus(item.invoice.id, newStatus)
                            }
                        },
                        onDeleteProject = {
                            projectViewModel.deleteProject(item.project.id)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        if (clients.isEmpty()) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(stringResource(R.string.projects_client_required_title)) },
                text = { Text(stringResource(R.string.projects_client_required_message)) },
                confirmButton = {
                    Button(
                        onClick = {
                            showAddDialog = false
                            onNavigateToClients()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen, contentColor = Color.Black)
                    ) {
                        Text(stringResource(R.string.projects_client_required_button), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text(stringResource(R.string.projects_client_required_cancel), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        } else {
            AddProjectDialog(
                clients = clients,
                onDismiss = { showAddDialog = false },
                onSave = { clientId, title, desc, days, amount ->
                    val deadlineTime = System.currentTimeMillis() + (days * 24L * 60 * 60 * 1000)
                    projectViewModel.createProjectWithInvoice(
                        clientId = clientId,
                        title = title,
                        description = desc,
                        deadline = deadlineTime,
                        amount = amount,
                        onSuccess = { showAddDialog = false }
                    )
                }
            )
        }
    }
}

@Composable
fun ProjectRowCard(
    item: ProjectListItem,
    onUpdateProjectStatus: (ProjectStatus) -> Unit,
    onUpdateInvoiceStatus: (InvoiceStatus) -> Unit,
    onDeleteProject: () -> Unit
) {
    val project = item.project
    val invoice = item.invoice

    val isProjectCompleted = project.status == ProjectStatus.Completed.name
    val isInvoiceOverdue = invoice != null &&
            (invoice.status == InvoiceStatus.Unpaid.name || invoice.status == InvoiceStatus.PendingBalance.name) &&
            System.currentTimeMillis() > invoice.dueDate

    val borderStrokeColor = if (isInvoiceOverdue) {
        WarningRed
    } else {
        MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("project_row_card_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (isInvoiceOverdue) 1.2.dp else 1.dp, borderStrokeColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.clientName.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MoneyGreen
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onDeleteProject,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.cd_projects_delete),
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = project.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.projects_deadline_label),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = if (isProjectCompleted) stringResource(R.string.projects_deadline_completed) else TimeFormatter.getDeadlineCountdown(project.deadline),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (!isProjectCompleted && project.deadline < System.currentTimeMillis()) WarningRed else MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.projects_invoice_label),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = if (invoice != null) com.example.ui.CurrencyUtils.format(invoice.totalAmount) else com.example.ui.CurrencyUtils.format(0.0),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isInvoiceOverdue) WarningRed else MoneyGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.projects_status_project),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    var showProjMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { showProjMenu = true },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = project.status,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showProjMenu,
                            onDismissRequest = { showProjMenu = false }
                        ) {
                            ProjectStatus.values().forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.name) },
                                    onClick = {
                                        onUpdateProjectStatus(status)
                                        showProjMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.projects_status_invoice),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    var showInvoiceMenu by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            onClick = { showInvoiceMenu = true },
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isInvoiceOverdue) WarningRed else MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = invoice?.status ?: stringResource(R.string.projects_none),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (invoice?.status == InvoiceStatus.FullyPaid.name) MoneyGreen else if (isInvoiceOverdue) WarningRed else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showInvoiceMenu,
                            onDismissRequest = { showInvoiceMenu = false }
                        ) {
                            InvoiceStatus.values().forEach { status ->
                                DropdownMenuItem(
                                    text = { Text(status.name) },
                                    onClick = {
                                        onUpdateInvoiceStatus(status)
                                        showInvoiceMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddProjectDialog(
    clients: List<Client>,
    onDismiss: () -> Unit,
    onSave: (clientId: String, title: String, desc: String, days: Int, amount: Double) -> Unit
) {
    var selectedClient by remember { mutableStateOf(clients.first()) }
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var daysAhead by remember { mutableStateOf(3) }
    var amount by remember { mutableStateOf("") }

    var expandedDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.projects_create_title), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column {
                    Text(
                        stringResource(R.string.projects_create_client_label),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text(selectedClient.name, color = MaterialTheme.colorScheme.onSurface)
                        }
                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            clients.forEach { client ->
                                DropdownMenuItem(
                                    text = { Text(client.name) },
                                    onClick = {
                                        selectedClient = client
                                        expandedDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.projects_create_title_label)) },
                    placeholder = { Text(stringResource(R.string.projects_create_title_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MoneyGreen,
                        focusedLabelColor = MoneyGreen
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text(stringResource(R.string.projects_create_desc_label)) },
                    placeholder = { Text(stringResource(R.string.projects_create_desc_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MoneyGreen,
                        focusedLabelColor = MoneyGreen
                    ),
                    maxLines = 3
                )

                Column {
                    Text(
                        stringResource(R.string.projects_create_deadline_format, daysAhead),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Slider(
                        value = daysAhead.toFloat(),
                        onValueChange = { daysAhead = it.toInt() },
                        valueRange = 1f..30f,
                        steps = 29,
                        colors = SliderDefaults.colors(
                            thumbColor = MoneyGreen,
                            activeTrackColor = MoneyGreen
                        )
                    )
                    Text(
                        text = stringResource(R.string.projects_create_deadline_hint),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.projects_create_amount_label)) },
                    placeholder = { Text(stringResource(R.string.projects_create_amount_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MoneyGreen,
                        focusedLabelColor = MoneyGreen
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            val amountNum = amount.toDoubleOrNull() ?: 0.0
            Button(
                onClick = {
                    if (title.isNotBlank() && amountNum > 0.0) {
                        onSave(selectedClient.id, title.trim(), desc.trim(), daysAhead, amountNum)
                    }
                },
                enabled = title.isNotBlank() && amountNum > 0.0,
                colors = ButtonDefaults.buttonColors(containerColor = MoneyGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_project_button")
            ) {
                Text(stringResource(R.string.projects_create_button), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_project_button")
            ) {
                Text(stringResource(R.string.projects_create_cancel), color = MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}
