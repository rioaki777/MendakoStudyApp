package com.rioaki.mendakostudyapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_state")
data class UserState(
    @PrimaryKey val id: Int = 1,
    val currentPoints: Int = 0,
    val equippedAccessories: String = "[]"
)
