package com.example.invoice

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.project.Project

enum class InvoiceStatus {
    Unpaid, DepositPaid, PendingBalance, FullyPaid
}

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class Invoice(
    @PrimaryKey val id: String,
    val projectId: String,
    val totalAmount: Double,
    val dueDate: Long,
    val status: String,
    val createdAt: Long
)
