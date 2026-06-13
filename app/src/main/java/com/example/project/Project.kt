package com.example.project

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.client.Client

enum class ProjectStatus {
    InProgress, PendingDelivery, Completed
}

@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["clientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["clientId"])]
)
data class Project(
    @PrimaryKey val id: String,
    val clientId: String,
    val title: String,
    val description: String,
    val status: String,
    val deadline: Long,
    val createdAt: Long
)
