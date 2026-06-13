package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.client.Client
import com.example.client.ClientDao
import com.example.client.TimelineEntry
import com.example.project.Project
import com.example.project.ProjectDao
import com.example.invoice.Invoice
import com.example.invoice.InvoiceDao
import androidx.room.TypeConverters

@Database(
    entities = [
        Client::class,
        TimelineEntry::class,
        Project::class,
        Invoice::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun projectDao(): ProjectDao
    abstract fun invoiceDao(): InvoiceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Migration from version 1 to 2:
         * Initial schema creation — no data transformation needed.
         * All future schema changes MUST add a new Migration step here.
         */
        /**
         * Migration from version 1 to 2:
         * Creates initial schema matching the Room entity definitions exactly.
         * Fixed: timeline_entries uses `content` (not `notes`) and omits `eventType`
         * to match the TimelineEntry entity.
         */
        /**
         * Migration from version 2 to 3:
         * No schema changes — version bump to align with entity definitions.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No schema changes in this version
            }
        }

        /**
         * Migration from version 3 to 4:
         * No schema changes — placeholder for future schema evolution.
         * Added to provide a safe upgrade path without destructive migration.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No schema changes in this version
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `clients` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `name` TEXT NOT NULL,
                        `contactChannel` TEXT NOT NULL,
                        `lastContactTimestamp` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `timeline_entries` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `clientId` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `content` TEXT NOT NULL DEFAULT ''
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `projects` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `clientId` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `deadline` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `invoices` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `projectId` TEXT NOT NULL,
                        `totalAmount` REAL NOT NULL,
                        `dueDate` INTEGER NOT NULL,
                        `status` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "creator_os_lite_db"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                // Last-resort fallback for unexpected version skew.
                .fallbackToDestructiveMigration()
                .fallbackToDestructiveMigrationOnDatabaseDowngrade()
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
