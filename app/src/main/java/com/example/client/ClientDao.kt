package com.example.client

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY createdAt DESC")
    fun getAllClients(): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getClientById(id: String): Client?

    @Query("SELECT * FROM clients WHERE id = :id")
    fun observeClientById(id: String): Flow<Client?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client)

    @Update
    suspend fun updateClient(client: Client)

    @Query("DELETE FROM clients WHERE id = :id")
    suspend fun deleteClientById(id: String)

    // Timeline Entry queries
    @Query("SELECT * FROM timeline_entries ORDER BY timestamp DESC")
    fun getAllTimelineEntries(): Flow<List<TimelineEntry>>

    @Query("SELECT * FROM timeline_entries WHERE clientId = :clientId ORDER BY timestamp DESC")
    fun getTimelineEntriesForClient(clientId: String): Flow<List<TimelineEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineEntry(entry: TimelineEntry)

    @Query("DELETE FROM timeline_entries WHERE id = :id")
    suspend fun deleteTimelineEntry(id: String)
}
