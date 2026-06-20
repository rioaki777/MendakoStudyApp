package com.rioaki.mendakostudyapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "owned_item")
data class OwnedItem(
    @PrimaryKey val itemId: Int,
    val quantity: Int = 1
)
