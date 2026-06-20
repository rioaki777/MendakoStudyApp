package com.rioaki.mendakostudyapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_item")
data class ShopItem(
    @PrimaryKey val id: Int,
    val name: String,
    val category: String,
    val price: Int,
    val imageResName: String
)
