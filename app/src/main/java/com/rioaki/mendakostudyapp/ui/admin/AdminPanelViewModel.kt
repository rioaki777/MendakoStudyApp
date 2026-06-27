package com.rioaki.mendakostudyapp.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.rioaki.mendakostudyapp.data.db.AppDatabase
import com.rioaki.mendakostudyapp.data.db.entity.HiraganaQuestion
import com.rioaki.mendakostudyapp.data.db.entity.UserState
import kotlinx.coroutines.launch

class AdminPanelViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).hiraganaQuestionDao()
    private val userStateDao = AppDatabase.getInstance(application).userStateDao()
    val questions = dao.observeAll()

    val currentPoints = userStateDao.observe().map { it?.currentPoints ?: 0 }

    val additionMaxAnswer = userStateDao.observe().map { it?.additionMaxAnswer ?: 10 }
    val subtractionMaxAnswer = userStateDao.observe().map { it?.subtractionMaxAnswer ?: 10 }

    fun setPoints(points: Int) = viewModelScope.launch {
        val clamped = points.coerceAtLeast(0)
        if (userStateDao.getOnce() == null) {
            userStateDao.upsert(UserState(currentPoints = clamped))
        } else {
            userStateDao.setPoints(clamped)
        }
    }

    fun setAdditionMaxAnswer(max: Int) = viewModelScope.launch {
        val clamped = max.coerceAtLeast(1)
        if (userStateDao.getOnce() == null) {
            userStateDao.upsert(UserState(additionMaxAnswer = clamped))
        } else {
            userStateDao.setAdditionMaxAnswer(clamped)
        }
    }

    fun setSubtractionMaxAnswer(max: Int) = viewModelScope.launch {
        val clamped = max.coerceAtLeast(1)
        if (userStateDao.getOnce() == null) {
            userStateDao.upsert(UserState(subtractionMaxAnswer = clamped))
        } else {
            userStateDao.setSubtractionMaxAnswer(clamped)
        }
    }

    fun addQuestion(text: String) = viewModelScope.launch {
        dao.insert(HiraganaQuestion(text = text))
    }

    fun updateQuestion(question: HiraganaQuestion, newText: String) = viewModelScope.launch {
        dao.update(question.copy(text = newText))
    }

    fun deleteQuestion(question: HiraganaQuestion) = viewModelScope.launch {
        dao.delete(question)
    }
}
