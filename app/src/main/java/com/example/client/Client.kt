package com.example.client

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ContactChannel {
    Discord, Telegram, Email, WhatsApp, Other
}

@Entity(tableName = "clients")
data class Client(
    @PrimaryKey val id: String,
    val name: String,
    val contactChannel: String, // String representation of ContactChannel enum
    val lastContactTimestamp: Long,
    val createdAt: Long
)
