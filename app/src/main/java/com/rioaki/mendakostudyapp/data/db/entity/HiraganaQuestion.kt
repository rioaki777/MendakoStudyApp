package com.rioaki.mendakostudyapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hiragana_question")
data class HiraganaQuestion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val attemptCount: Int = 0,
    val correctCount: Int = 0
)
