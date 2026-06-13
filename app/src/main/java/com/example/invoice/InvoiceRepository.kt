package com.example.invoice

import kotlinx.coroutines.flow.Flow

class InvoiceRepository(private val invoiceDao: InvoiceDao) {
    val allInvoices: Flow<List<Invoice>> = invoiceDao.getAllInvoices()

    suspend fun getInvoiceForProject(projectId: String): Invoice? {
        return invoiceDao.getInvoiceForProject(projectId)
    }

    fun observeInvoiceForProject(projectId: String): Flow<Invoice?> {
        return invoiceDao.observeInvoiceForProject(projectId)
    }

    suspend fun insertInvoice(invoice: Invoice) {
        invoiceDao.insertInvoice(invoice)
    }

    suspend fun deleteInvoice(id: String) {
        invoiceDao.deleteInvoiceById(id)
    }
}
