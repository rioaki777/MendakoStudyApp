package com.rioaki.mendakostudyapp.data.seed

import com.rioaki.mendakostudyapp.data.db.AppDatabase
import com.rioaki.mendakostudyapp.data.db.entity.HiraganaQuestion
import com.rioaki.mendakostudyapp.data.db.entity.LessonStats
import com.rioaki.mendakostudyapp.data.db.entity.MendakoCharacterState
import com.rioaki.mendakostudyapp.data.db.entity.ShopItem
import com.rioaki.mendakostudyapp.data.db.entity.UserState
import com.rioaki.mendakostudyapp.data.model.MendakoCatalog
import com.rioaki.mendakostudyapp.data.model.SubjectType

object SeedData {

    suspend fun seed(db: AppDatabase) {
        seedUserState(db)
        seedLessonStats(db)
        seedShopItems(db)
        seedHiraganaQuestions(db)
        seedMendakoCharacters(db)
    }

    private suspend fun seedUserState(db: AppDatabase) {
        if (db.userStateDao().getOnce() == null) {
            db.userStateDao().upsert(UserState())
        }
    }

    private suspend fun seedLessonStats(db: AppDatabase) {
        SubjectType.values().forEach { subject ->
            if (db.lessonStatsDao().getBySubject(subject.name) == null) {
                db.lessonStatsDao().upsert(LessonStats(subject = subject.name))
            }
        }
    }

    private suspend fun seedShopItems(db: AppDatabase) {
        if (db.shopItemDao().getCount() > 0) return
        db.shopItemDao().insertAll(
            listOf(
                // ごはん
                ShopItem(1, "おさかな", "FOOD", 20, "item_food"),
                ShopItem(2, "かに", "FOOD", 30, "item_food"),
                ShopItem(3, "えび", "FOOD", 40, "item_food"),
                // アクセサリー
                ShopItem(4, "ぼうし", "ACCESSORY", 60, "item_accessory"),
                ShopItem(5, "マフラー", "ACCESSORY", 80, "item_accessory"),
                ShopItem(6, "リボン", "ACCESSORY", 50, "item_accessory"),
                // かぐ
                ShopItem(7, "テーブル", "FURNITURE", 100, "item_furniture"),
                ShopItem(8, "ベッド", "FURNITURE", 150, "item_furniture"),
                ShopItem(9, "ソファ", "FURNITURE", 120, "item_furniture")
            )
        )
    }

    private suspend fun seedMendakoCharacters(db: AppDatabase) {
        // カタログ各個体の行を冪等に作成する（既存行は維持）。
        // デフォルト(id=0)は最初からアンロック済み、友達は未アンロック。
        MendakoCatalog.all.forEach { def ->
            db.mendakoCharacterStateDao().insert(
                MendakoCharacterState(
                    id = def.id,
                    unlocked = def.id == MendakoCatalog.DEFAULT_ID
                )
            )
        }
    }

    private suspend fun seedHiraganaQuestions(db: AppDatabase) {
        if (db.hiraganaQuestionDao().getCount() > 0) return
        listOf("いぬ", "ねこ", "さかな", "りんご", "そら", "はな", "くも").forEach { text ->
            db.hiraganaQuestionDao().insert(HiraganaQuestion(text = text))
        }
    }
}
