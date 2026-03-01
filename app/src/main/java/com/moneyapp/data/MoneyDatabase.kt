package com.moneyapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Event::class, Transaction::class, Fund::class, LinkingGroup::class],
    version = 1,
    exportSchema = false
)
abstract class MoneyDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun transactionDao(): TransactionDao
    abstract fun fundDao(): FundDao
    abstract fun linkingGroupDao(): LinkingGroupDao

    companion object {
        @Volatile
        private var INSTANCE: MoneyDatabase? = null

        fun getDatabase(context: Context): MoneyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoneyDatabase::class.java,
                    "money_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
