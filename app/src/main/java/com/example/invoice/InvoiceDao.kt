package com.example.invoice

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY dueDate ASC")
    fun getAllInvoices(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE projectId = :projectId LIMIT 1")
    suspend fun getInvoiceForProject(projectId: String): Invoice?

    @Query("SELECT * FROM invoices WHERE projectId = :projectId LIMIT 1")
    fun observeInvoiceForProject(projectId: String): Flow<Invoice?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoiceById(id: String)
}
