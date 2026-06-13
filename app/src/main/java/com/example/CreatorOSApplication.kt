package com.example

import android.app.Application
import com.example.data.AppDatabase
import com.example.client.ClientRepository
import com.example.project.ProjectRepository
import com.example.invoice.InvoiceRepository
import com.example.ui.SettingsManager

class CreatorOSApplication : Application() {
    lateinit var clientRepository: ClientRepository
    lateinit var projectRepository: ProjectRepository
    lateinit var invoiceRepository: InvoiceRepository
    lateinit var settingsManager: SettingsManager

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        clientRepository = ClientRepository(database.clientDao())
        projectRepository = ProjectRepository(database.projectDao())
        invoiceRepository = InvoiceRepository(database.invoiceDao())
        settingsManager = SettingsManager.getInstance(this)
    }
}
