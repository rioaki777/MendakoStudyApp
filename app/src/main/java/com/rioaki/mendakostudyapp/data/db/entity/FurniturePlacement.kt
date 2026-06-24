package com.rioaki.mendakostudyapp.data.db.entity

import androidx.room.Entity

/**
 * 家具の配置。メンダコ個体ごとに別々の部屋を持つため、主キーは (mendakoId, itemId) の複合キー。
 * [x] / [y] は部屋サイズに対する比率(0〜1)。
 */
@Entity(tableName = "furniture_placement", primaryKeys = ["mendakoId", "itemId"])
data class FurniturePlacement(
    val mendakoId: Int,
    val itemId: Int,
    val x: Float,
    val y: Float
)
