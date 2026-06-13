package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.client.Client
import com.example.client.TimelineEntry
import com.example.invoice.Invoice
import com.example.project.Project
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility for exporting all DB data to CSV files for sharing/backup.
 */
object DataExporter {

    private fun writeCsv(file: File, headers: List<String>, rows: List<List<String>>) {
        file.bufferedWriter(charset = Charsets.UTF_8).use { writer ->
            // Write UTF-8 BOM for Excel compatibility with Chinese characters
            writer.write("\uFEFF")
            writer.write(headers.joinToString(",") { escapeCsv(it) })
            writer.newLine()
            for (row in rows) {
                writer.write(row.joinToString(",") { escapeCsv(it) })
                writer.newLine()
            }
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun formatTimestamp(ts: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        return sdf.format(Date(ts))
    }

    /**
     * Export all data to CSV files in the cache directory and return the directory URI.
     */
    fun exportAll(
        context: Context,
        clients: List<Client>,
        timelineEntries: List<TimelineEntry>,
        projects: List<Project>,
        invoices: List<Invoice>
    ): File {
        val exportDir = File(context.cacheDir, "creator_os_export_${System.currentTimeMillis()}")
        exportDir.mkdirs()

        // Clients
        writeCsv(
            File(exportDir, "clients.csv"),
            listOf("id", "name", "contactChannel", "lastContact", "createdAt"),
            clients.map { c ->
                listOf(c.id, c.name, c.contactChannel, formatTimestamp(c.lastContactTimestamp), formatTimestamp(c.createdAt))
            }
        )

        // Timeline entries
        writeCsv(
            File(exportDir, "timeline_entries.csv"),
            listOf("id", "clientId", "timestamp", "content"),
            timelineEntries.map { e ->
                listOf(e.id, e.clientId, formatTimestamp(e.timestamp), e.content)
            }
        )

        // Projects
        writeCsv(
            File(exportDir, "projects.csv"),
            listOf("id", "clientId", "title", "description", "status", "deadline", "createdAt"),
            projects.map { p ->
                listOf(p.id, p.clientId, p.title, p.description, p.status, formatTimestamp(p.deadline), formatTimestamp(p.createdAt))
            }
        )

        // Invoices
        writeCsv(
            File(exportDir, "invoices.csv"),
            listOf("id", "projectId", "totalAmount", "dueDate", "status", "createdAt"),
            invoices.map { inv ->
                listOf(inv.id, inv.projectId, inv.totalAmount.toString(), formatTimestamp(inv.dueDate), inv.status, formatTimestamp(inv.createdAt))
            }
        )

        return exportDir
    }

    /**
     * Share the export directory via a chooser intent.
     */
    fun shareExport(context: Context, exportDir: File) {
        val uris = mutableListOf<Uri>()
        val files = exportDir.listFiles() ?: return

        for (file in files) {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            uris.add(uri)
        }

        if (uris.isEmpty()) return

        val sendIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "text/csv"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_SUBJECT, "Creator OS Lite Data Export")
        }

        val chooser = Intent.createChooser(sendIntent, "Share CSV Data")
        context.startActivity(chooser)
    }
}
