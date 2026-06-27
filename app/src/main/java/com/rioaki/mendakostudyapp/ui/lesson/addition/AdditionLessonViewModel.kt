package com.rioaki.mendakostudyapp.ui.lesson.addition

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rioaki.mendakostudyapp.data.db.AppDatabase
import com.rioaki.mendakostudyapp.data.db.entity.LessonStats
import com.rioaki.mendakostudyapp.data.model.SubjectType
import com.rioaki.mendakostudyapp.util.AdaptiveDifficulty
import com.rioaki.mendakostudyapp.util.AdditionQuestion
import com.rioaki.mendakostudyapp.util.PointCalculator
import com.rioaki.mendakostudyapp.util.QuestionGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class AnswerFeedback(val selectedIndex: Int, val correct: Boolean, val correctAnswer: Int)
data class LessonComplete(val correctCount: Int, val totalCount: Int, val earnedPoints: Int)

class AdditionLessonViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val QUESTION_COUNT = 5
    }

    private val db = AppDatabase.getInstance(application)
    private val questions = mutableListOf<AdditionQuestion>()
    private val results = mutableListOf<Boolean>()

    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> = _currentIndex

    private val _currentQuestion = MutableLiveData<AdditionQuestion?>()
    val currentQuestion: LiveData<AdditionQuestion?> = _currentQuestion

    private val _answerFeedback = MutableLiveData<AnswerFeedback?>()
    val answerFeedback: LiveData<AnswerFeedback?> = _answerFeedback

    private val _lessonComplete = MutableLiveData<LessonComplete?>()
    val lessonComplete: LiveData<LessonComplete?> = _lessonComplete

    private var choicesEnabled = true

    init {
        viewModelScope.launch {
            // 難度の自動調整はせず、単純ランダムで出題する。
            // 答えの上限は管理画面の設定（既定 10）を使う。
            val maxAnswer = db.userStateDao().getOnce()?.additionMaxAnswer ?: 10
            questions.addAll(QuestionGenerator.generateAdditionSet(maxAnswer, QUESTION_COUNT))
            _currentQuestion.value = questions[0]
        }
    }

    fun submitAnswer(choiceIndex: Int) {
        if (!choicesEnabled) return
        val question = _currentQuestion.value ?: return
        val selected = question.choices[choiceIndex]
        val correct = selected == question.answer
        results.add(correct)
        choicesEnabled = false

        _answerFeedback.value = AnswerFeedback(choiceIndex, correct, question.answer)

        viewModelScope.launch {
            delay(1200)
            _answerFeedback.value = null
            val nextIndex = (_currentIndex.value ?: 0) + 1
            if (nextIndex >= QUESTION_COUNT) {
                saveStats()
                val earned = PointCalculator.calc(results.count { it }, QUESTION_COUNT)
                _lessonComplete.value = LessonComplete(results.count { it }, QUESTION_COUNT, earned)
            } else {
                _currentIndex.value = nextIndex
                _currentQuestion.value = questions[nextIndex]
                choicesEnabled = true
            }
        }
    }

    private suspend fun saveStats() {
        val stats = db.lessonStatsDao().getBySubject(SubjectType.ADDITION.name)
            ?: LessonStats(subject = SubjectType.ADDITION.name)
        val newResults = AdaptiveDifficulty.appendResults(stats.recentResults, results)
        db.lessonStatsDao().upsert(
            stats.copy(
                recentResults = newResults,
                totalAttempts = stats.totalAttempts + QUESTION_COUNT,
                totalCorrect = stats.totalCorrect + results.count { it }
            )
        )
    }
}
