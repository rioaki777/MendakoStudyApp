package com.rioaki.mendakostudyapp.ui.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rioaki.mendakostudyapp.data.db.AppDatabase
import com.rioaki.mendakostudyapp.data.db.entity.HiraganaQuestion
import kotlinx.coroutines.launch

class AdminPanelViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).hiraganaQuestionDao()
    val questions = dao.observeAll()

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
