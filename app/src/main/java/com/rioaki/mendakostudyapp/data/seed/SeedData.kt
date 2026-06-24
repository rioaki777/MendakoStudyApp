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
        // upsertAll は同一idの既存行を最新定義へ上書きする（冪等・自己修復）。
        // 旧バージョンで古い imageResName 等がシードされた端末でも、起動時に正しい定義へ揃う。
        db.shopItemDao().upsertAll(
            listOf(
                // ごはん
                ShopItem(1, "おさかな", "FOOD", 20, "item_fish"),
                ShopItem(2, "かに", "FOOD", 30, "item_crab"),
                ShopItem(3, "えび", "FOOD", 40, "item_shrimp"),
                ShopItem(10, "ケーキ", "FOOD", 50, "item_cake"),
                ShopItem(11, "アイス", "FOOD", 45, "item_icecream"),
                ShopItem(12, "いか", "FOOD", 35, "item_squid"),
                // アクセサリー
                ShopItem(4, "ぼうし", "ACCESSORY", 60, "item_hat"),
                ShopItem(5, "マフラー", "ACCESSORY", 80, "item_scarf"),
                ShopItem(6, "リボン", "ACCESSORY", 50, "item_ribbon"),
                ShopItem(16, "めがね", "ACCESSORY", 70, "item_glasses"),
                ShopItem(17, "おうかん", "ACCESSORY", 120, "item_crown"),
                ShopItem(18, "ヘッドホン", "ACCESSORY", 90, "item_headphone"),
                // かぐ
                ShopItem(7, "テーブル", "FURNITURE", 100, "item_table"),
                ShopItem(8, "ベッド", "FURNITURE", 150, "item_bed"),
                ShopItem(9, "ソファ", "FURNITURE", 120, "item_sofa"),
                ShopItem(13, "かびん", "FURNITURE", 80, "item_vase"),
                ShopItem(14, "ランプ", "FURNITURE", 90, "item_lamp"),
                ShopItem(15, "ほんだな", "FURNITURE", 130, "item_bookshelf")
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
