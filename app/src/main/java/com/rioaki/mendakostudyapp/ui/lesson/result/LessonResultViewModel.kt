package com.rioaki.mendakostudyapp.ui.lesson.result

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.rioaki.mendakostudyapp.data.db.AppDatabase
import com.rioaki.mendakostudyapp.data.db.entity.PointHistory
import kotlinx.coroutines.launch

class LessonResultViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private var pointsAwarded = false

    val totalPoints: LiveData<Int> = db.userStateDao().observe().map { it?.currentPoints ?: 0 }

    private val _awardDone = MutableLiveData(false)
    val awardDone: LiveData<Boolean> = _awardDone

    fun awardPoints(earnedPoints: Int, subjectType: String) {
        if (pointsAwarded || earnedPoints <= 0) {
            _awardDone.value = true
            return
        }
        pointsAwarded = true
        viewModelScope.launch {
            db.userStateDao().addPoints(earnedPoints)
            db.pointHistoryDao().insert(
                PointHistory(type = "EARN", amount = earnedPoints, subject = subjectType)
            )
            _awardDone.value = true
        }
    }
}
