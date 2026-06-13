package com.example.dashboard

import com.example.client.Client
import com.example.project.Project
import com.example.project.ProjectStatus
import com.example.invoice.Invoice
import com.example.invoice.InvoiceStatus
import kotlin.math.abs

enum class RiskSeverity {
    MEDIUM, HIGH, CRITICAL
}

sealed class RiskItem(
    val id: String,
    val severity: RiskSeverity,
    val title: String,
    val details: String,
    val timestamp: Long
) {
    class CriticalClient(
        val clientId: String,
        val clientName: String,
        val daysSinceContact: Int,
        val pendingAmount: Double,
        timestamp: Long
    ) : RiskItem(
        id = "critical_$clientId",
        severity = RiskSeverity.CRITICAL,
        title = "⚠ Critical Client: $clientName",
        details = "$daysSinceContact Days No Contact • ${com.example.ui.CurrencyUtils.format(pendingAmount)} Pending",
        timestamp = timestamp
    )

    class OverdueInvoice(
        val invoiceId: String,
        val projectTitle: String,
        val amount: Double,
        val daysOverdue: Int,
        timestamp: Long
    ) : RiskItem(
        id = "invoice_$invoiceId",
        severity = RiskSeverity.HIGH,
        title = "⚠ Payment Overdue",
        details = "$projectTitle • Overdue by $daysOverdue Days • ${com.example.ui.CurrencyUtils.format(amount)}",
        timestamp = timestamp
    )

    class ExpiringProject(
        val projectId: String,
        val projectTitle: String,
        val hoursRemaining: Int,
        val isOverdue: Boolean,
        timestamp: Long
    ) : RiskItem(
        id = "project_$projectId",
        severity = RiskSeverity.HIGH,
        title = "⚠ Expiring Project",
        details = "$projectTitle • " + if (isOverdue) "${abs(hoursRemaining)} hours overdue" else "$hoursRemaining hours left",
        timestamp = timestamp
    )

    class GhostedClient(
        val clientId: String,
        val clientName: String,
        val hoursSinceContact: Int,
        timestamp: Long
    ) : RiskItem(
        id = "ghosted_$clientId",
        severity = RiskSeverity.MEDIUM,
        title = "⚠ Ghosted Client: $clientName",
        details = "$hoursSinceContact Hours No Contact",
        timestamp = timestamp
    )
}

object RiskEngine {

    fun generateRisks(
        clients: List<Client>,
        projects: List<Project>,
        invoices: List<Invoice>,
        currentTime: Long = System.currentTimeMillis()
    ): List<RiskItem> {
        val risks = mutableListOf<RiskItem>()

        // 1. Identify which clients have active projects
        // Project statuses: InProgress, PendingDelivery, Completed
        val clientActiveProjectIds = projects
            .filter { it.status == ProjectStatus.InProgress.name || it.status == ProjectStatus.PendingDelivery.name }
            .groupBy { it.clientId }

        // Find Ghosted Clients:
        // CurrentTime - LastContactTimestamp > 48 hours AND Client has at least one active project
        val ghostedClients = clients.filter { client ->
            val hasActiveProj = clientActiveProjectIds.containsKey(client.id)
            val timeDiff = currentTime - client.lastContactTimestamp
            hasActiveProj && (timeDiff > 48L * 60 * 60 * 1000)
        }

        // Find Overdue Invoices:
        // (Status == PendingBalance OR Status == Unpaid) AND CurrentTime > DueDate
        val overdueInvoices = invoices.filter { invoice ->
            val isOverdueStatus = invoice.status == InvoiceStatus.Unpaid.name || invoice.status == InvoiceStatus.PendingBalance.name
            isOverdueStatus && currentTime > invoice.dueDate
        }

        // We mapped client ID -> total overdue amount for resolving Critical Client cases
        val overdueInvoicesByClientId = mutableMapOf<String, MutableList<Invoice>>()
        for (invoice in overdueInvoices) {
            val proj = projects.find { it.id == invoice.projectId }
            if (proj != null) {
                overdueInvoicesByClientId.getOrPut(proj.clientId) { mutableListOf() }.add(invoice)
            }
        }

        // Track escalated ids so we don't display dual cards
        val escalatedClientIds = mutableSetOf<String>()
        val handledInvoiceIds = mutableSetOf<String>()

        // Escalation: Critical Client = Ghosted Client AND Overdue Invoice for the same client
        for (gClient in ghostedClients) {
            val clientOverdueInvoices = overdueInvoicesByClientId[gClient.id]
            if (clientOverdueInvoices != null && clientOverdueInvoices.isNotEmpty()) {
                val totalOverdueAmount = clientOverdueInvoices.sumOf { it.totalAmount }
                val timeDiff = currentTime - gClient.lastContactTimestamp
                val daysSinceContact = (timeDiff / (24L * 60 * 60 * 1000)).toInt()

                risks.add(
                    RiskItem.CriticalClient(
                        clientId = gClient.id,
                        clientName = gClient.name,
                        daysSinceContact = daysSinceContact,
                        pendingAmount = totalOverdueAmount,
                        timestamp = gClient.lastContactTimestamp
                    )
                )
                escalatedClientIds.add(gClient.id)
                handledInvoiceIds.addAll(clientOverdueInvoices.map { it.id })
            }
        }

        // Add remaining ghosted clients (who don't have overdue invoices)
        for (gClient in ghostedClients) {
            if (gClient.id !in escalatedClientIds) {
                val timeDiff = currentTime - gClient.lastContactTimestamp
                val hoursSinceContact = (timeDiff / (60L * 60 * 1000)).toInt()
                risks.add(
                    RiskItem.GhostedClient(
                        clientId = gClient.id,
                        clientName = gClient.name,
                        hoursSinceContact = hoursSinceContact,
                        timestamp = gClient.lastContactTimestamp
                    )
                )
            }
        }

        // Add remaining overdue invoices (where the client is not ghosted)
        for (invoice in overdueInvoices) {
            if (invoice.id !in handledInvoiceIds) {
                val proj = projects.find { it.id == invoice.projectId }
                val projectTitle = proj?.title ?: "Unknown Project"
                val timeDiff = currentTime - invoice.dueDate
                val daysOverdue = (timeDiff / (24L * 60 * 60 * 1000)).toInt()

                risks.add(
                    RiskItem.OverdueInvoice(
                        invoiceId = invoice.id,
                        projectTitle = projectTitle,
                        amount = invoice.totalAmount,
                        daysOverdue = daysOverdue,
                        timestamp = invoice.dueDate
                    )
                )
            }
        }

        // Find Expiring Projects:
        // Deadline - CurrentTime < 72 hours AND Status != Completed
        val expiringProjects = projects.filter { proj ->
            val isNotCompleted = proj.status != ProjectStatus.Completed.name
            val timeDiffToDeadline = proj.deadline - currentTime
            isNotCompleted && (timeDiffToDeadline < 72L * 60 * 60 * 1000)
        }

        for (proj in expiringProjects) {
            val timeDiff = proj.deadline - currentTime
            val hoursRemaining = (timeDiff / (60L * 60 * 1000)).toInt()
            val isOverdue = timeDiff < 0

            risks.add(
                RiskItem.ExpiringProject(
                    projectId = proj.id,
                    projectTitle = proj.title,
                    hoursRemaining = hoursRemaining,
                    isOverdue = isOverdue,
                    timestamp = proj.deadline
                )
            )
        }

        // Sort risks by Priority: Critical -> High -> Medium
        return risks.sortedBy {
            when (it.severity) {
                RiskSeverity.CRITICAL -> 0
                RiskSeverity.HIGH -> 1
                RiskSeverity.MEDIUM -> 2
            }
        }
    }
}
