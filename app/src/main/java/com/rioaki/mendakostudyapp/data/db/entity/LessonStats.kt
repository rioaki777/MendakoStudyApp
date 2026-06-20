package com.rioaki.mendakostudyapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lesson_stats")
data class LessonStats(
    @PrimaryKey val subject: String,
    val recentResults: String = "[]",
    val totalAttempts: Int = 0,
    val totalCorrect: Int = 0
)
