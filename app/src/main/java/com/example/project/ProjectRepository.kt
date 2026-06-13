package com.example.project

import kotlinx.coroutines.flow.Flow

class ProjectRepository(private val projectDao: ProjectDao) {
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()

    suspend fun getProjectById(id: String): Project? {
        return projectDao.getProjectById(id)
    }

    fun getProjectsForClient(clientId: String): Flow<List<Project>> {
        return projectDao.getProjectsForClient(clientId)
    }

    suspend fun insertProject(project: Project) {
        projectDao.insertProject(project)
    }

    suspend fun deleteProject(id: String) {
        projectDao.deleteProjectById(id)
    }
}
