package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.BookmarkEntity
import com.example.data.model.DownloadEntity
import com.example.data.model.WatchHistoryEntity

@Database(
    entities = [
        DownloadEntity::class,
        WatchHistoryEntity::class,
        BookmarkEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
    abstract fun historyDao(): HistoryDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "streamvault_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
