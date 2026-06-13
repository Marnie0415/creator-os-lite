package com.example.dashboard

import com.example.client.Client
import com.example.invoice.Invoice
import com.example.project.Project
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for [RiskEngine] — the core risk calculation logic.
 *
 * Covers:
 * - Ghosted client detection (48h no contact + active project)
 * - Overdue invoice detection (Unpaid/PendingBalance past due date)
 * - Critical client (ghosted + overdue combo)
 * - Expiring project detection (72h window)
 * - Edge cases (exactly at boundary, no data, all completed)
 */
class RiskEngineTest {

    private val now = 1_000_000_000_000L // a fixed reference point
    private val hourMs = 60L * 60 * 1000
    private val dayMs = 24L * hourMs

    // Helper: create a client with lastContactTimestamp
    private fun makeClient(
        id: String = "c1",
        name: String = "Test Client",
        lastContact: Long = now,
        channel: String = "Email"
    ) = Client(
        id = id,
        name = name,
        contactChannel = channel,
        lastContactTimestamp = lastContact,
        createdAt = now
    )

    // Helper: create a project
    private fun makeProject(
        id: String = "p1",
        clientId: String = "c1",
        title: String = "Test Project",
        status: String = "InProgress",
        deadline: Long = now + 7 * dayMs
    ) = Project(
        id = id,
        clientId = clientId,
        title = title,
        description = "",
        status = status,
        deadline = deadline,
        createdAt = now
    )

    // Helper: create an invoice
    private fun makeInvoice(
        id: String = "inv1",
        projectId: String = "p1",
        totalAmount: Double = 500.0,
        status: String = "Unpaid",
        dueDate: Long = now + 30 * dayMs
    ) = Invoice(
        id = id,
        projectId = projectId,
        totalAmount = totalAmount,
        dueDate = dueDate,
        status = status,
        createdAt = now
    )

    // ==================== Happy Path ====================

    @Test
    fun `empty data returns empty risks`() {
        val risks = RiskEngine.generateRisks(
            clients = emptyList(),
            projects = emptyList(),
            invoices = emptyList(),
            currentTime = now
        )
        assertTrue("No data should produce no risks", risks.isEmpty())
    }

    @Test
    fun `client with recent contact no active project yields no risk`() {
        val client = makeClient(lastContact = now - 10 * hourMs)
        val project = makeProject(status = "Completed")
        val risks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = emptyList(),
            currentTime = now
        )
        assertTrue("All-ok client should not appear in risk feed", risks.isEmpty())
    }

    // ==================== Ghosted Client ====================

    @Test
    fun `client with 48h no contact and active project is ghosted`() {
        val client = makeClient(lastContact = now - 49 * hourMs)
        val project = makeProject(clientId = "c1")
        val risks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = emptyList(),
            currentTime = now
        )
        assertEquals(1, risks.size)
        assertTrue("Risk should be GhostedClient", risks[0] is RiskItem.GhostedClient)
    }

    @Test
    fun `client with exactly 48h no contact is NOT ghosted (boundary)`() {
        val client = makeClient(lastContact = now - 48 * hourMs)
        val project = makeProject(clientId = "c1")
        val risks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = emptyList(),
            currentTime = now
        )
        assertTrue("48h exactly should not trigger ghosting (strictly greater)", risks.isEmpty())
    }

    @Test
    fun `ghosted client without active project does not trigger`() {
        val client = makeClient(lastContact = now - 100 * hourMs)
        val project = makeProject(clientId = "c1", status = "Completed")
        val risks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = emptyList(),
            currentTime = now
        )
        assertTrue("Completed project should not trigger ghosting", risks.isEmpty())
    }

    // ==================== Overdue Invoice ====================

    @Test
    fun `overdue unpaid invoice triggers OverdueInvoice risk`() {
        val client = makeClient()
        val project = makeProject(clientId = "c1")
        val invoice = makeInvoice(projectId = "p1", dueDate = now - 5 * dayMs)
        val risks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = listOf(invoice),
            currentTime = now
        )
        assertTrue("Overdue invoice should create a risk", risks.isNotEmpty())
        assertTrue("Risk should be OverdueInvoice", risks[0] is RiskItem.OverdueInvoice)
    }

    @Test
    fun `unpaid invoice not yet due does not trigger`() {
        val client = makeClient()
        val project = makeProject(clientId = "c1")
        val invoice = makeInvoice(projectId = "p1", dueDate = now + 5 * dayMs)
        val risks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = listOf(invoice),
            currentTime = now
        )
        assertTrue("Not-yet-due invoice should not trigger", risks.isEmpty())
    }

    @Test
    fun `fully paid overdue invoice does not trigger`() {
        val client = makeClient()
        val project = makeProject(clientId = "c1")
        val invoice = makeInvoice(projectId = "p1", status = "FullyPaid", dueDate = now - 5 * dayMs)
        val risks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = listOf(invoice),
            currentTime = now
        )
        assertTrue("FullyPaid should not trigger even if past due date", risks.isEmpty())
    }

    @Test
    fun `deposit paid overdue invoice does not trigger`() {
        val client = makeClient()
        val project = makeProject(clientId = "c1")
        val invoice = makeInvoice(projectId = "p1", status = "DepositPaid", dueDate = now - 5 * dayMs)
        val risks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = listOf(invoice),
            currentTime = now
        )
        assertTrue("DepositPaid should not trigger", risks.isEmpty())
    }

    // ==================== Critical Client (ghosted + overdue) ====================

    @Test
    fun `ghosted client with overdue invoice becomes CriticalClient`() {
        val client = makeClient(lastContact = now - 100 * hourMs)
        val project = makeProject(clientId = "c1")
        val invoice = makeInvoice(projectId = "p1", dueDate = now - 5 * dayMs)
        val risks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = listOf(invoice),
            currentTime = now
        )
        assertEquals(1, risks.size)
        assertTrue("Ghosted + Overdue combo should be CriticalClient", risks[0] is RiskItem.CriticalClient)
        assertEquals(RiskSeverity.CRITICAL, risks[0].severity)
    }

    @Test
    fun `critical client dedupes ghosted and overdue for same client`() {
        val client = makeClient(lastContact = now - 100 * hourMs)
        val p1 = makeProject(id = "p1", clientId = "c1")
        val p2 = makeProject(id = "p2", clientId = "c1", title = "Second Project")
        val inv1 = makeInvoice(projectId = "p1", dueDate = now - 5 * dayMs)
        val inv2 = makeInvoice(id = "inv2", projectId = "p2", dueDate = now - 3 * dayMs)
        val risks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(p1, p2),
            invoices = listOf(inv1, inv2),
            currentTime = now
        )
        // Should produce 1 CriticalClient (not 1 Ghosted + 2 Overdue)
        assertEquals(1, risks.size)
        assertTrue(risks[0] is RiskItem.CriticalClient)
    }

    // ==================== Expiring Project ====================

    @Test
    fun `project expiring within 72 hours triggers ExpiringProject risk`() {
        val client = makeClient()
        val project = makeProject(deadline = now + 24 * hourMs)
        val risks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = emptyList(),
            currentTime = now
        )
        assertEquals(1, risks.size)
        assertTrue("Project due within 72h should create ExpiringProject", risks[0] is RiskItem.ExpiringProject)
    }

    @Test
    fun `project exactly 72 hours away does NOT trigger (boundary)`() {
        val client = makeClient()
        val project = makeProject(deadline = now + 72 * hourMs)
        val risks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = emptyList(),
            currentTime = now
        )
        assertTrue("Exactly 72h should not trigger (strictly less than)", risks.isEmpty())
    }

    @Test
    fun `completed project does not trigger even if deadline passed`() {
        val client = makeClient()
        val project = makeProject(status = "Completed", deadline = now - 24 * hourMs)
        val risks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = emptyList(),
            currentTime = now
        )
        assertTrue("Completed project should not trigger expiring", risks.isEmpty())
    }

    @Test
    fun `overdue project triggers ExpiringProject with negative hours`() {
        val client = makeClient()
        val project = makeProject(deadline = now - 10 * hourMs)
        val risks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = emptyList(),
            currentTime = now
        )
        assertEquals(1, risks.size)
        val expiring = risks[0] as RiskItem.ExpiringProject
        assertTrue("Overdue project should show negative hours", expiring.hoursRemaining < 0)
        assertTrue("Overdue project isOverdue should be true", expiring.isOverdue)
    }

    // ==================== Priority Ordering ====================

    // ==================== Configurable Thresholds ====================

    @Test
    fun `custom ghost hours threshold changes ghost detection`() {
        val client = makeClient(lastContact = now - 30 * hourMs) // 30h, less than default 48h
        val project = makeProject(clientId = "c1")
        // With default 48h threshold, 30h should NOT be ghosted
        val defaultRisks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = emptyList(),
            currentTime = now,
            ghostHours = 48
        )
        assertTrue("30h with 48h threshold should not ghost", defaultRisks.isEmpty())

        // With custom 24h threshold, 30h SHOULD be ghosted
        val customRisks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = emptyList(),
            currentTime = now,
            ghostHours = 24
        )
        assertTrue("30h with 24h threshold should ghost", customRisks.isNotEmpty())
        assertTrue(customRisks[0] is RiskItem.GhostedClient)
    }

    @Test
    fun `custom expiring hours threshold changes expiring detection`() {
        val client = makeClient()
        val project = makeProject(deadline = now + 48 * hourMs) // 48h away
        // With default 72h threshold, 48h SHOULD trigger
        val defaultRisks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = emptyList(),
            currentTime = now,
            expiringHours = 72
        )
        assertTrue("48h with 72h threshold should trigger expiring", defaultRisks.isNotEmpty())

        // With custom 24h threshold, 48h should NOT trigger
        val customRisks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = emptyList(),
            currentTime = now,
            expiringHours = 24
        )
        assertTrue("48h with 24h threshold should not trigger", customRisks.isEmpty())
    }

    @Test
    fun `minimum ghost threshold of 6 hours still works`() {
        val client = makeClient(lastContact = now - 7 * hourMs) // 7h
        val project = makeProject(clientId = "c1")
        val risks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = emptyList(),
            currentTime = now,
            ghostHours = 6
        )
        assertTrue("7h with 6h threshold should ghost", risks.isNotEmpty())
    }

    @Test
    fun `maximum threshold of 168 hours works for expiring`() {
        val client = makeClient()
        val project = makeProject(deadline = now + 100 * hourMs)
        val risks = RiskEngine.generateRisks(
            clients = listOf(client),
            projects = listOf(project),
            invoices = emptyList(),
            currentTime = now,
            expiringHours = 168
        )
        assertTrue("100h with 168h threshold should trigger", risks.isNotEmpty())
    }

    @Test
    fun `risks are sorted by severity Critical first then High then Medium`() {
        // Critical client
        val ghostClient = makeClient(id = "c1", lastContact = now - 100 * hourMs)
        val project1 = makeProject(id = "p1", clientId = "c1")
        val invoice1 = makeInvoice(projectId = "p1", dueDate = now - 5 * dayMs)

        // Overdue invoice (High)
        val okClient = makeClient(id = "c2", name = "OK Client", lastContact = now)
        val project2 = makeProject(id = "p2", clientId = "c2")
        val invoice2 = makeInvoice(id = "inv2", projectId = "p2", dueDate = now - 2 * dayMs)

        val risks = RiskEngine.generateRisks(
            clients = listOf(ghostClient, okClient),
            projects = listOf(project1, project2),
            invoices = listOf(invoice1, invoice2),
            currentTime = now
        )

        assertTrue("First risk should be CRITICAL", risks[0].severity == RiskSeverity.CRITICAL)
        assertTrue("Second risk should be HIGH", risks[1].severity == RiskSeverity.HIGH)
    }
}
