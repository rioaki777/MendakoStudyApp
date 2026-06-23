package com.rioaki.mendakostudyapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        HiraganaQuestion::class,
        MendakoCharacterState::class
    ],
    version = 2,
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
    abstract fun mendakoCharacterStateDao(): MendakoCharacterStateDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        /**
         * v1 → v2: メンダコ個体機能の追加。
         * - user_state に activeMendakoId 列を追加
         * - mendako_character テーブルを新設
         * - デフォルト個体(id=0)を unlocked=1 で作成し、既存の装備を引き継ぐ
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE user_state ADD COLUMN activeMendakoId INTEGER NOT NULL DEFAULT 0"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS mendako_character (" +
                        "id INTEGER PRIMARY KEY NOT NULL, " +
                        "unlocked INTEGER NOT NULL DEFAULT 0, " +
                        "equippedAccessories TEXT NOT NULL DEFAULT '[]')"
                )
                db.execSQL(
                    "INSERT OR IGNORE INTO mendako_character (id, unlocked, equippedAccessories) " +
                        "SELECT 0, 1, COALESCE(equippedAccessories, '[]') FROM user_state WHERE id = 1"
                )
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mendako_db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
    }
}
