package com.example.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.client.ClientRepository
import com.example.data.NotificationHelper
import com.example.invoice.InvoiceRepository
import com.example.invoice.InvoiceStatus
import com.example.project.ProjectRepository
import com.example.ui.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    application: Application,
    clientRepository: ClientRepository,
    projectRepository: ProjectRepository,
    invoiceRepository: InvoiceRepository,
    private val settingsManager: SettingsManager? = null
) : AndroidViewModel(application) {

    val uiState: StateFlow<DashboardUiState> = combine(
        clientRepository.allClients,
        projectRepository.allProjects,
        invoiceRepository.allInvoices
    ) { clients, projects, invoices ->
        val currentTime = System.currentTimeMillis()
        val ghostH = settingsManager?.ghostHours?.value?.toLong() ?: 48L
        val expiringH = settingsManager?.expiringHours?.value?.toLong() ?: 72L

        // 1. Calculate Risk List from RiskEngine
        val risks = RiskEngine.generateRisks(clients, projects, invoices, currentTime, ghostH, expiringH)

        // 2. Count Clients Pending Reply (= Ghosted Clients count)
        val clientsPendingReplyCount = risks.count { it is RiskItem.GhostedClient || it is RiskItem.CriticalClient }

        // 3. Count Pending Balance Invoices (= Unpaid or PendingBalance)
        val pendingBalanceInvoicesCount = invoices.count {
            it.status == InvoiceStatus.Unpaid.name || it.status == InvoiceStatus.PendingBalance.name
        }

        // 4. Count Expiring Projects (= Projects due within 72 hours and not Completed)
        // Let's filter projects that are due within 72 hours and not completed
        val expiringProjectsCount = projects.count { proj ->
            val isNotCompleted = proj.status != com.example.project.ProjectStatus.Completed.name
            val isExpiring = (proj.deadline - currentTime) < expiringH * 60 * 60 * 1000
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

        // Post notifications for active risks (debounced to once per 24h)
        if (risks.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                NotificationHelper.postRiskNotifications(getApplication(), risks)
            }
        }

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
