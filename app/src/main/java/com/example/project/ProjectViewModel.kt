package com.example.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client.ClientRepository
import com.example.invoice.Invoice
import com.example.invoice.InvoiceRepository
import com.example.invoice.InvoiceStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class ProjectListItem(
    val project: Project,
    val clientName: String,
    val invoice: Invoice?
)

class ProjectViewModel(
    private val projectRepository: ProjectRepository,
    private val clientRepository: ClientRepository,
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    // All projects combined with client and invoice info
    val projectsList: StateFlow<List<ProjectListItem>> = combine(
        projectRepository.allProjects,
        clientRepository.allClients,
        invoiceRepository.allInvoices
    ) { projects, clients, invoices ->
        projects.map { project ->
            val client = clients.find { it.id == project.clientId }
            val invoice = invoices.find { it.projectId == project.id }
            ProjectListItem(
                project = project,
                clientName = client?.name ?: "Unknown Client",
                invoice = invoice
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Creates a project and its corresponding invoice in a single atomic transaction helper.
     */
    fun createProjectWithInvoice(
        clientId: String,
        title: String,
        description: String,
        deadline: Long,
        amount: Double,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val projectId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            val newProject = Project(
                id = projectId,
                clientId = clientId,
                title = title,
                description = description,
                status = ProjectStatus.InProgress.name,
                deadline = deadline,
                createdAt = now
            )
            projectRepository.insertProject(newProject)

            val newInvoice = Invoice(
                id = UUID.randomUUID().toString(),
                projectId = projectId,
                totalAmount = amount,
                dueDate = deadline,
                status = InvoiceStatus.Unpaid.name,
                createdAt = now
            )
            invoiceRepository.insertInvoice(newInvoice)

            val formattedAmount = com.example.ui.CurrencyUtils.format(amount)
            val client = clientRepository.getClientById(clientId)
            if (client != null) {
                val timelineContent = "Project '$title' started. Invoice created with outstanding balance of $formattedAmount."
                val timelineEntry = com.example.client.TimelineEntry(
                    id = UUID.randomUUID().toString(),
                    clientId = clientId,
                    timestamp = now,
                    content = timelineContent
                )
                clientRepository.insertTimelineEntry(timelineEntry)
            }

            onSuccess()
        }
    }

    /**
     * Update project status & trigger timeline updates
     */
    fun updateProjectStatus(projectId: String, status: ProjectStatus) {
        viewModelScope.launch {
            val proj = projectRepository.getProjectById(projectId) ?: return@launch
            val updated = proj.copy(status = status.name)
            projectRepository.insertProject(updated)

            val client = clientRepository.getClientById(proj.clientId)
            if (client != null) {
                val now = System.currentTimeMillis()
                val timelineEntry = com.example.client.TimelineEntry(
                    id = UUID.randomUUID().toString(),
                    clientId = proj.clientId,
                    timestamp = now,
                    content = "Project status updated to ${status.name} for '${proj.title}'"
                )
                clientRepository.insertTimelineEntry(timelineEntry)
            }
        }
    }

    fun updateProject(
        projectId: String,
        title: String,
        description: String,
        deadline: Long,
        amount: Double
    ) {
        viewModelScope.launch {
            val proj = projectRepository.getProjectById(projectId) ?: return@launch
            val updated = proj.copy(
                title = title,
                description = description,
                deadline = deadline
            )
            projectRepository.insertProject(updated)

            // Always update the invoice for this project (each project has one invoice)
            val inv = invoiceRepository.getInvoiceForProject(projectId)
            if (inv != null) {
                val updatedInv = inv.copy(totalAmount = amount)
                invoiceRepository.insertInvoice(updatedInv)
            }

            // Log to timeline and update client lastContactTimestamp
            val now = System.currentTimeMillis()
            val client = clientRepository.getClientById(proj.clientId)
            if (client != null) {
                val timelineEntry = com.example.client.TimelineEntry(
                    id = java.util.UUID.randomUUID().toString(),
                    clientId = proj.clientId,
                    timestamp = now,
                    content = "Project '$title' was updated (details/amount edited)."
                )
                clientRepository.insertTimelineEntry(timelineEntry)
            }
        }
    }

    fun deleteProject(id: String) {
        viewModelScope.launch {
            val proj = projectRepository.getProjectById(id)
            if (proj != null) {
                val invoice = invoiceRepository.getInvoiceForProject(id)
                if (invoice != null) {
                    invoiceRepository.deleteInvoice(invoice.id)
                }
                projectRepository.deleteProject(id)
            }
        }
    }
}
