package com.example.invoicegen.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CompanySettings::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "invoice_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Initial data is handled by the DAO or via a manual insert if needed, 
                        // but since id=1 is fixed in CompanySettings, it will be saved on first update.
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
