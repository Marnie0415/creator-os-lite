package com.example.client

import kotlinx.coroutines.flow.Flow

class ClientRepository(private val clientDao: ClientDao) {
    val allClients: Flow<List<Client>> = clientDao.getAllClients()
    val allTimelineEntries: Flow<List<TimelineEntry>> = clientDao.getAllTimelineEntries()

    fun observeClientById(id: String): Flow<Client?> {
        return clientDao.observeClientById(id)
    }

    suspend fun getClientById(id: String): Client? {
        return clientDao.getClientById(id)
    }

    suspend fun insertClient(client: Client) {
        clientDao.insertClient(client)
    }

    suspend fun updateClient(client: Client) {
        clientDao.updateClient(client)
    }

    suspend fun deleteClient(id: String) {
        clientDao.deleteClientById(id)
    }

    fun getTimelineEntriesForClient(clientId: String): Flow<List<TimelineEntry>> {
        return clientDao.getTimelineEntriesForClient(clientId)
    }

    suspend fun insertTimelineEntry(entry: TimelineEntry) {
        clientDao.insertTimelineEntry(entry)
        val client = clientDao.getClientById(entry.clientId)
        if (client != null) {
            val updatedClient = client.copy(lastContactTimestamp = entry.timestamp)
            clientDao.insertClient(updatedClient)
        }
    }

    suspend fun deleteTimelineEntry(id: String) {
        clientDao.deleteTimelineEntry(id)
    }
}
