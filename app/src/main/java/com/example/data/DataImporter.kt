package com.example.data

import android.content.Context
import android.net.Uri
import com.example.client.Client
import com.example.client.ClientDao
import com.example.client.ContactChannel
import com.example.client.TimelineEntry
import com.example.invoice.Invoice
import com.example.invoice.InvoiceDao
import com.example.project.Project
import com.example.project.ProjectDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

/**
 * Imports CSV data exported by [DataExporter].
 * Reads from content:// URIs (FileProvider shares).
 */
object DataImporter {

    data class ImportResult(
        val clientsImported: Int = 0,
        val timelinesImported: Int = 0,
        val projectsImported: Int = 0,
        val invoicesImported: Int = 0,
        val errors: List<String> = emptyList()
    )

    fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current.clear()
                }
                else -> current.append(ch)
            }
        }
        result.add(current.toString().trim())
        return result
    }

    suspend fun importAll(
        context: Context,
        clientsUri: Uri?,
        timelinesUri: Uri?,
        projectsUri: Uri?,
        invoicesUri: Uri?,
        clientDao: ClientDao,
        projectDao: com.example.project.ProjectDao,
        invoiceDao: com.example.invoice.InvoiceDao
    ): ImportResult = withContext(Dispatchers.IO) {
        val errors = mutableListOf<String>()
        var clientsCount = 0
        var timelinesCount = 0
        var projectsCount = 0
        var invoicesCount = 0

        try {
            if (clientsUri != null) {
                val reader = BufferedReader(InputStreamReader(context.contentResolver.openInputStream(clientsUri)))
                val lines = reader.readLines()
                for (i in 1 until lines.size) { // skip header
                    try {
                        val cols = parseCsvLine(lines[i])
                        if (cols.size >= 5) {
                            val client = Client(
                                id = cols[0].ifBlank { UUID.randomUUID().toString() },
                                name = cols[1],
                                contactChannel = runCatching { ContactChannel.valueOf(cols[2]) }.getOrDefault(ContactChannel.Other).name,
                                lastContactTimestamp = cols[3].toLongOrNull() ?: System.currentTimeMillis(),
                                createdAt = cols[4].toLongOrNull() ?: System.currentTimeMillis()
                            )
                            clientDao.insertClient(client)
                            clientsCount++
                        }
                    } catch (e: Exception) {
                        errors.add("Clients row $i: ${e.localizedMessage}")
                    }
                }
                reader.close()
            }
        } catch (e: Exception) {
            errors.add("Clients file: ${e.localizedMessage}")
        }

        try {
            if (timelinesUri != null) {
                val reader = BufferedReader(InputStreamReader(context.contentResolver.openInputStream(timelinesUri)))
                val lines = reader.readLines()
                for (i in 1 until lines.size) {
                    try {
                        val cols = parseCsvLine(lines[i])
                        if (cols.size >= 4) {
                            val entry = TimelineEntry(
                                id = cols[0].ifBlank { UUID.randomUUID().toString() },
                                clientId = cols[1],
                                timestamp = cols[2].toLongOrNull() ?: System.currentTimeMillis(),
                                content = cols[3]
                            )
                            clientDao.insertTimelineEntry(entry)
                            timelinesCount++
                        }
                    } catch (e: Exception) {
                        errors.add("Timeline row $i: ${e.localizedMessage}")
                    }
                }
                reader.close()
            }
        } catch (e: Exception) {
            errors.add("Timeline file: ${e.localizedMessage}")
        }

        try {
            if (projectsUri != null) {
                val reader = BufferedReader(InputStreamReader(context.contentResolver.openInputStream(projectsUri)))
                val lines = reader.readLines()
                for (i in 1 until lines.size) {
                    try {
                        val cols = parseCsvLine(lines[i])
                        if (cols.size >= 6) {
                            val project = Project(
                                id = cols[0].ifBlank { UUID.randomUUID().toString() },
                                clientId = cols[1],
                                title = cols[2],
                                description = cols.getOrElse(3) { "" },
                                status = cols.getOrElse(4) { "InProgress" },
                                deadline = cols[5].toLongOrNull() ?: System.currentTimeMillis(),
                                createdAt = cols.getOrElse(6) { "${System.currentTimeMillis()}" }.toLongOrNull() ?: System.currentTimeMillis()
                            )
                            projectDao.insertProject(project)
                            projectsCount++
                        }
                    } catch (e: Exception) {
                        errors.add("Projects row $i: ${e.localizedMessage}")
                    }
                }
                reader.close()
            }
        } catch (e: Exception) {
            errors.add("Projects file: ${e.localizedMessage}")
        }

        try {
            if (invoicesUri != null) {
                val reader = BufferedReader(InputStreamReader(context.contentResolver.openInputStream(invoicesUri)))
                val lines = reader.readLines()
                for (i in 1 until lines.size) {
                    try {
                        val cols = parseCsvLine(lines[i])
                        if (cols.size >= 5) {
                            val invoice = Invoice(
                                id = cols[0].ifBlank { UUID.randomUUID().toString() },
                                projectId = cols[1],
                                totalAmount = cols[2].toDoubleOrNull() ?: 0.0,
                                dueDate = cols[3].toLongOrNull() ?: System.currentTimeMillis(),
                                status = cols.getOrElse(4) { "Unpaid" },
                                createdAt = cols.getOrElse(5) { "${System.currentTimeMillis()}" }.toLongOrNull() ?: System.currentTimeMillis()
                            )
                            invoiceDao.insertInvoice(invoice)
                            invoicesCount++
                        }
                    } catch (e: Exception) {
                        errors.add("Invoices row $i: ${e.localizedMessage}")
                    }
                }
                reader.close()
            }
        } catch (e: Exception) {
            errors.add("Invoices file: ${e.localizedMessage}")
        }

        ImportResult(clientsCount, timelinesCount, projectsCount, invoicesCount, errors)
    }
}
