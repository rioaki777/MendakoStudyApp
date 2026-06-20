package com.rioaki.mendakostudyapp.ui.lesson.selection

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import com.rioaki.mendakostudyapp.data.db.AppDatabase
import com.rioaki.mendakostudyapp.data.model.SubjectType
import com.rioaki.mendakostudyapp.util.AdaptiveDifficulty

class LessonSelectionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val additionAccuracy = db.lessonStatsDao().observe(SubjectType.ADDITION.name)
        .map { AdaptiveDifficulty.calcAccuracyPercent(it?.recentResults ?: "[]") }

    val subtractionAccuracy = db.lessonStatsDao().observe(SubjectType.SUBTRACTION.name)
        .map { AdaptiveDifficulty.calcAccuracyPercent(it?.recentResults ?: "[]") }

    val hiraganaAccuracy = db.lessonStatsDao().observe(SubjectType.HIRAGANA.name)
        .map { AdaptiveDifficulty.calcAccuracyPercent(it?.recentResults ?: "[]") }
}
