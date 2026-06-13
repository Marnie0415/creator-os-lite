package com.example.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.ProjectRepository
import com.example.invoice.InvoiceRepository
import com.example.invoice.InvoiceStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class ClientListItem(
    val client: Client,
    val activeProjectsCount: Int,
    val outstandingRevenue: Double
)

data class ClientDetailState(
    val client: Client? = null,
    val projects: List<com.example.project.Project> = emptyList(),
    val timeline: List<TimelineEntry> = emptyList(),
    val outstandingRevenue: Double = 0.0
)

class ClientViewModel(
    private val clientRepository: ClientRepository,
    private val projectRepository: ProjectRepository,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    // 1. Observe all client items with active project counts and outstanding invoices revenue integrated
    val clientsList: StateFlow<List<ClientListItem>> = combine(
        clientRepository.allClients,
        projectRepository.allProjects,
        invoiceRepository.allInvoices
    ) { clients, projects, invoices ->
        clients.map { client ->
            val clientProjects = projects.filter { it.clientId == client.id }
            val activeCount = clientProjects.count {
                it.status == com.example.project.ProjectStatus.InProgress.name ||
                it.status == com.example.project.ProjectStatus.PendingDelivery.name
            }
            
            val outstandingAmount = clientProjects.sumOf { proj ->
                val invoice = invoices.find { it.projectId == proj.id }
                if (invoice != null && (invoice.status == InvoiceStatus.Unpaid.name || invoice.status == InvoiceStatus.PendingBalance.name)) {
                    invoice.totalAmount
                } else {
                    0.0
                }
            }

            ClientListItem(
                client = client,
                activeProjectsCount = activeCount,
                outstandingRevenue = outstandingAmount
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // State for the currently selected client (for detail view)
    private val _selectedClientId = MutableStateFlow<String?>(null)
    val selectedClientId: StateFlow<String?> = _selectedClientId

    val clientDetail: StateFlow<ClientDetailState?> = _selectedClientId.flatMapLatest { clientId ->
        if (clientId == null) {
            flowOf(null)
        } else {
            combine(
                clientRepository.observeClientById(clientId),
                projectRepository.getProjectsForClient(clientId),
                clientRepository.getTimelineEntriesForClient(clientId),
                invoiceRepository.allInvoices
            ) { client, projects, timeline, invoices ->
                if (client == null) return@combine null

                val outstanding = projects.sumOf { proj ->
                    val invoice = invoices.find { it.projectId == proj.id }
                    if (invoice != null && (invoice.status == InvoiceStatus.Unpaid.name || invoice.status == InvoiceStatus.PendingBalance.name)) {
                        invoice.totalAmount
                    } else {
                        0.0
                    }
                }

                ClientDetailState(
                    client = client,
                    projects = projects,
                    timeline = timeline,
                    outstandingRevenue = outstanding
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun selectClient(clientId: String?) {
        _selectedClientId.value = clientId
    }

    fun createClient(name: String, contactChannel: ContactChannel) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val newClient = Client(
                id = UUID.randomUUID().toString(),
                name = name,
                contactChannel = contactChannel.name,
                lastContactTimestamp = now,
                createdAt = now
            )
            clientRepository.insertClient(newClient)
            
            // Add an initial timeline entry
            addTimelineEntry(newClient.id, "Client created on Creator OS Lite via ${contactChannel.name}", now)
        }
    }

    fun deleteClient(clientId: String) {
        viewModelScope.launch {
            clientRepository.deleteClient(clientId)
            if (_selectedClientId.value == clientId) {
                _selectedClientId.value = null
            }
        }
    }

    fun addTimelineEntry(clientId: String, content: String, timestamp: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val entry = TimelineEntry(
                id = UUID.randomUUID().toString(),
                clientId = clientId,
                timestamp = timestamp,
                content = content
            )
            clientRepository.insertTimelineEntry(entry)
        }
    }

    fun deleteTimelineEntry(id: String) {
        viewModelScope.launch {
            clientRepository.deleteTimelineEntry(id)
        }
    }
}
