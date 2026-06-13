package com.example.invoice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client.ClientRepository
import com.example.project.ProjectRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class InvoiceViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val projectRepository: ProjectRepository,
    private val clientRepository: ClientRepository
) : ViewModel() {

    val invoicesList: StateFlow<List<Invoice>> = invoiceRepository.allInvoices.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateInvoiceStatus(invoiceId: String, newStatus: InvoiceStatus) {
        viewModelScope.launch {
            // Find invoice
            val allInvoices = invoiceRepository.allInvoices.first()
            val invoice = allInvoices.find { it.id == invoiceId } ?: return@launch
            
            val updated = invoice.copy(status = newStatus.name)
            invoiceRepository.insertInvoice(updated)

            // Auto log status update to timeline
            val project = projectRepository.getProjectById(invoice.projectId)
            if (project != null) {
                val client = clientRepository.getClientById(project.clientId)
                if (client != null) {
                    val now = System.currentTimeMillis()
                    val formattedAmount = com.example.ui.CurrencyUtils.format(invoice.totalAmount)
                    val statusText = when (newStatus) {
                        InvoiceStatus.Unpaid -> "Unpaid"
                        InvoiceStatus.DepositPaid -> "Deposit Paid"
                        InvoiceStatus.PendingBalance -> "Pending Balance"
                        InvoiceStatus.FullyPaid -> "Fully Paid"
                    }
                    val timelineEntry = com.example.client.TimelineEntry(
                        id = UUID.randomUUID().toString(),
                        clientId = project.clientId,
                        timestamp = now,
                        content = "Invoice for '${project.title}' is now marked as $statusText ($formattedAmount)"
                    )
                    clientRepository.insertTimelineEntry(timelineEntry)
                }
            }
        }
    }
}
