package com.rioaki.mendakostudyapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "point_history")
data class PointHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val amount: Int,
    val subject: String? = null,
    val itemId: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)
