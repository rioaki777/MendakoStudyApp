package com.rioaki.mendakostudyapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_state")
data class UserState(
    @PrimaryKey val id: Int = 1,
    val currentPoints: Int = 0,
    // 個体ごとの装備は mendako_character テーブルへ移行したため、この列は後方互換用に残している（未使用）。
    val equippedAccessories: String = "[]",
    // 現在操作中のメンダコ個体ID（0 = デフォルト）。
    val activeMendakoId: Int = 0,
    // 足し算の答えの上限（管理画面で設定、既定は 10）。
    val additionMaxAnswer: Int = 10,
    // 引き算の答えの上限（管理画面で設定、既定は 10）。
    val subtractionMaxAnswer: Int = 10
)
