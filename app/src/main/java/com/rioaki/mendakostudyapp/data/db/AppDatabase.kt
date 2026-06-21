package com.rioaki.mendakostudyapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rioaki.mendakostudyapp.data.db.dao.*
import com.rioaki.mendakostudyapp.data.db.entity.*

@Database(
    entities = [
        UserState::class,
        LessonStats::class,
        PointHistory::class,
        ShopItem::class,
        OwnedItem::class,
        FurniturePlacement::class,
        HiraganaQuestion::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userStateDao(): UserStateDao
    abstract fun lessonStatsDao(): LessonStatsDao
    abstract fun pointHistoryDao(): PointHistoryDao
    abstract fun shopItemDao(): ShopItemDao
    abstract fun ownedItemDao(): OwnedItemDao
    abstract fun furniturePlacementDao(): FurniturePlacementDao
    abstract fun hiraganaQuestionDao(): HiraganaQuestionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mendako_db"
                ).build().also { INSTANCE = it }
            }
    }
}
