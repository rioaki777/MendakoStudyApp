package com.rioaki.mendakostudyapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "furniture_placement")
data class FurniturePlacement(
    @PrimaryKey val itemId: Int,
    val x: Float,
    val y: Float
)
