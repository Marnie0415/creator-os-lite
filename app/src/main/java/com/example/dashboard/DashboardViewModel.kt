package com.example.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.client.ClientRepository
import com.example.project.ProjectRepository
import com.example.invoice.InvoiceRepository
import com.example.invoice.InvoiceStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val clientsPendingReplyCount: Int = 0,
    val pendingBalanceInvoicesCount: Int = 0,
    val expiringProjectsCount: Int = 0,
    val collectedRevenue: Double = 0.0,
    val pendingRevenue: Double = 0.0,
    val riskFeed: List<RiskItem> = emptyList(),
    val isEmpty: Boolean = true
)

class DashboardViewModel(
    clientRepository: ClientRepository,
    projectRepository: ProjectRepository,
    invoiceRepository: InvoiceRepository
) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> = combine(
        clientRepository.allClients,
        projectRepository.allProjects,
        invoiceRepository.allInvoices
    ) { clients, projects, invoices ->
        val currentTime = System.currentTimeMillis()

        // 1. Calculate Risk List from RiskEngine
        val risks = RiskEngine.generateRisks(clients, projects, invoices, currentTime)

        // 2. Count Clients Pending Reply (= Ghosted Clients count)
        val clientsPendingReplyCount = risks.count { it is RiskItem.GhostedClient || it is RiskItem.CriticalClient }

        // 3. Count Pending Balance Invoices (= Invoices where status != FullyPaid)
        val pendingBalanceInvoicesCount = invoices.count { it.status != InvoiceStatus.FullyPaid.name }

        // 4. Count Expiring Projects (= Projects due within 72 hours and not Completed)
        // Let's filter projects that are due within 72 hours and not completed
        val expiringProjectsCount = projects.count { proj ->
            val isNotCompleted = proj.status != com.example.project.ProjectStatus.Completed.name
            val isExpiring = (proj.deadline - currentTime) < 72L * 60 * 60 * 1000
            isNotCompleted && isExpiring
        }

        // 5. Collected Revenue = Sum of FullyPaid invoices
        val collectedRevenue = invoices
            .filter { it.status == InvoiceStatus.FullyPaid.name }
            .sumOf { it.totalAmount }

        // 6. Pending Revenue = Sum of Unpaid + PendingBalance invoices
        val pendingRevenue = invoices
            .filter { it.status == InvoiceStatus.Unpaid.name || it.status == InvoiceStatus.PendingBalance.name }
            .sumOf { it.totalAmount }

        val isEmpty = clients.isEmpty() && projects.isEmpty() && invoices.isEmpty()

        DashboardUiState(
            clientsPendingReplyCount = clientsPendingReplyCount,
            pendingBalanceInvoicesCount = pendingBalanceInvoicesCount,
            expiringProjectsCount = expiringProjectsCount,
            collectedRevenue = collectedRevenue,
            pendingRevenue = pendingRevenue,
            riskFeed = risks,
            isEmpty = isEmpty
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}
