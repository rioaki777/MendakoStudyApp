package com.rioaki.mendakostudyapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * メンダコ個体ごとの動的状態。
 * アンロック有無と、その個体に装備しているアクセサリー（既存と同じ "[4,5]" 形式のJSON）。
 */
@Entity(tableName = "mendako_character")
data class MendakoCharacterState(
    @PrimaryKey val id: Int,
    val unlocked: Boolean = false,
    val equippedAccessories: String = "[]"
)
