package com.example.data

import androidx.room.TypeConverter
import com.example.client.ContactChannel
import com.example.invoice.InvoiceStatus
import com.example.project.ProjectStatus

/**
 * Room TypeConverters for safe enum serialization.
 * Stores enums as their string names and validates on read.
 */
class Converters {

    @TypeConverter
    fun fromProjectStatus(status: ProjectStatus): String = status.name

    @TypeConverter
    fun toProjectStatus(value: String): ProjectStatus {
        return try {
            ProjectStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ProjectStatus.InProgress // safe default
        }
    }

    @TypeConverter
    fun fromInvoiceStatus(status: InvoiceStatus): String = status.name

    @TypeConverter
    fun toInvoiceStatus(value: String): InvoiceStatus {
        return try {
            InvoiceStatus.valueOf(value)
        } catch (e: IllegalArgumentException) {
            InvoiceStatus.Unpaid // safe default
        }
    }

    @TypeConverter
    fun fromContactChannel(channel: ContactChannel): String = channel.name

    @TypeConverter
    fun toContactChannel(value: String): ContactChannel {
        return try {
            ContactChannel.valueOf(value)
        } catch (e: IllegalArgumentException) {
            ContactChannel.Other // safe default
        }
    }
}
