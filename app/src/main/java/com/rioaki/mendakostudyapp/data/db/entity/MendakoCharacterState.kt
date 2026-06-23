package com.rioaki.mendakostudyapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * メンダコ個体ごとの動的状態。
 * アンロック有無と、その個体に装備しているアクセサリー（既存と同じ "[4,5]" 形式のJSON）。
 *
 * [accessoryPositions] は個体ごとのアクセサリー表示位置。
 * 形式は `{"4":[fx,fy],"5":[...]}` で、値は描画コンテナ幅/高さに対するオフセット比率
 * （レイアウト既定位置からのずれ。0 で既定位置）。
 */
@Entity(tableName = "mendako_character")
data class MendakoCharacterState(
    @PrimaryKey val id: Int,
    val unlocked: Boolean = false,
    val equippedAccessories: String = "[]",
    val accessoryPositions: String = "{}"
)
