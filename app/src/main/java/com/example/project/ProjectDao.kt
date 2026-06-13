package com.example.project

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY deadline ASC")
    fun getAllProjects(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: String): Project?

    @Query("SELECT * FROM projects WHERE clientId = :clientId ORDER BY deadline ASC")
    fun getProjectsForClient(clientId: String): Flow<List<Project>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
}
