package com.rioaki.mendakostudyapp.ui.lesson.hiragana

import android.app.Application
import android.graphics.PointF
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.rioaki.mendakostudyapp.data.db.AppDatabase
import com.rioaki.mendakostudyapp.data.db.entity.HiraganaQuestion
import com.rioaki.mendakostudyapp.data.db.entity.LessonStats
import com.rioaki.mendakostudyapp.data.model.SubjectType
import com.rioaki.mendakostudyapp.data.stroke.HiraganaStrokeData
import com.rioaki.mendakostudyapp.data.stroke.StrokeRepository
import com.rioaki.mendakostudyapp.util.AdaptiveDifficulty
import com.rioaki.mendakostudyapp.util.StrokeOrderJudge
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class CanvasState(
    val guideChar: Char,
    val strokeData: HiraganaStrokeData?,
    val completedStrokes: List<List<PointF>>,
    val nextStrokeIndex: Int
)

sealed class StrokeFeedback {
    object Correct : StrokeFeedback()
    object Wrong : StrokeFeedback()
}

data class CharComplete(val hadError: Boolean)
data class HiraganaLessonComplete(val correctCount: Int, val totalCount: Int, val earnedPoints: Int)

class HiraganaLessonViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    private var loadedQuestion: HiraganaQuestion? = null
    private var chars: List<Char> = emptyList()
    private val charResults = mutableListOf<Boolean>()

    private var currentCharIdx = 0
    private val strokesForCurrentChar = mutableListOf<List<PointF>>()
    // 結果画面表示用: 各画を 0〜1 正規化して蓄積（現在の文字分）
    private val normalizedStrokesForCurrentChar = mutableListOf<List<PointF>>()
    // 結果画面表示用: 書き終えた文字ごとの線
    private val drawnChars = mutableListOf<HiraganaResultHolder.DrawnChar>()
    private var hadErrorOnCurrentChar = false
    private var inputEnabled = true

    private val _displayText = MutableLiveData<String>()
    val displayText: LiveData<String> = _displayText

    private val _currentCharIndex = MutableLiveData(0)
    val currentCharIndex: LiveData<Int> = _currentCharIndex

    private val _totalCharCount = MutableLiveData(0)
    val totalCharCount: LiveData<Int> = _totalCharCount

    private val _canvasState = MutableLiveData<CanvasState?>()
    val canvasState: LiveData<CanvasState?> = _canvasState

    private val _strokeFeedback = MutableLiveData<StrokeFeedback?>()
    val strokeFeedback: LiveData<StrokeFeedback?> = _strokeFeedback

    private val _charComplete = MutableLiveData<CharComplete?>()
    val charComplete: LiveData<CharComplete?> = _charComplete

    private val _lessonComplete = MutableLiveData<HiraganaLessonComplete?>()
    val lessonComplete: LiveData<HiraganaLessonComplete?> = _lessonComplete

    private var started = false

    /**
     * 練習する問題を読み込む。[questionId] が [HiraganaSelectFragment.RANDOM_QUESTION_ID]
     * （おまかせ）または見つからない場合は、練習回数の少ない問題を自動で選ぶ。
     * 画面回転などで再度呼ばれても最初の1回だけ有効。
     */
    fun start(questionId: Int) {
        if (started) return
        started = true
        viewModelScope.launch {
            StrokeRepository.load(getApplication<Application>().assets)
            val dao = db.hiraganaQuestionDao()
            val q = (if (questionId > 0) dao.getById(questionId) else null)
                ?: dao.getNextQuestion()
            if (q != null) {
                loadedQuestion = q
                chars = q.text.filter { it != ' ' }.toList()
                _displayText.value = q.text.replace(' ', '\n')
                _totalCharCount.value = chars.size
                _currentCharIndex.value = 0
                updateCanvasState()
            }
        }
    }

    fun onStrokeCompleted(stroke: List<PointF>, canvasWidth: Int, canvasHeight: Int) {
        if (!inputEnabled || chars.isEmpty()) return
        inputEnabled = false

        val currentChar = chars.getOrNull(currentCharIdx) ?: return
        val strokeData = StrokeRepository.get(currentChar)
        val expectedStrokeIndex = strokesForCurrentChar.size
        val expectedStroke = strokeData?.strokes?.getOrNull(expectedStrokeIndex)

        val isCorrect = if (expectedStroke != null) {
            StrokeOrderJudge.judgeStroke(stroke, expectedStroke, canvasWidth, canvasHeight)
        } else {
            true
        }

        if (isCorrect) {
            strokesForCurrentChar.add(stroke)
            if (canvasWidth > 0 && canvasHeight > 0) {
                normalizedStrokesForCurrentChar.add(
                    stroke.map { PointF(it.x / canvasWidth, it.y / canvasHeight) }
                )
            }
            _strokeFeedback.value = StrokeFeedback.Correct

            val totalExpected = strokeData?.strokes?.size ?: 1
            if (strokesForCurrentChar.size >= totalExpected) {
                drawnChars.add(
                    HiraganaResultHolder.DrawnChar(
                        currentChar, normalizedStrokesForCurrentChar.toList()
                    )
                )
                viewModelScope.launch {
                    delay(200)
                    _strokeFeedback.value = null
                    updateCanvasState()
                    delay(100)
                    _charComplete.value = CharComplete(!hadErrorOnCurrentChar)
                    delay(800)
                    _charComplete.value = null
                    advanceToNextChar()
                    inputEnabled = true
                }
            } else {
                viewModelScope.launch {
                    delay(200)
                    _strokeFeedback.value = null
                    updateCanvasState()
                    inputEnabled = true
                }
            }
        } else {
            // 書き順を間違えたら画は受け付けず、同じ画を正しく書き直すまで先へ進めない。
            hadErrorOnCurrentChar = true
            _strokeFeedback.value = StrokeFeedback.Wrong
            viewModelScope.launch {
                delay(300)
                _strokeFeedback.value = null
                inputEnabled = true
            }
        }
    }

    fun onRetry() {
        if (!inputEnabled) return
        strokesForCurrentChar.clear()
        normalizedStrokesForCurrentChar.clear()
        hadErrorOnCurrentChar = false
        updateCanvasState()
    }

    private fun advanceToNextChar() {
        charResults.add(!hadErrorOnCurrentChar)
        currentCharIdx++
        strokesForCurrentChar.clear()
        normalizedStrokesForCurrentChar.clear()
        hadErrorOnCurrentChar = false

        if (currentCharIdx >= chars.size) {
            viewModelScope.launch {
                saveStats()
                val correct = charResults.count { it }
                val total = charResults.size
                // ひらがなは書ききった文字数分ポイント（書き順を間違えても書ききればOK）
                val points = total
                HiraganaResultHolder.lastResult = drawnChars.toList()
                _lessonComplete.value = HiraganaLessonComplete(correct, total, points)
            }
        } else {
            _currentCharIndex.value = currentCharIdx
            updateCanvasState()
        }
    }

    private fun updateCanvasState() {
        val currentChar = chars.getOrNull(currentCharIdx) ?: return
        _canvasState.value = CanvasState(
            guideChar = currentChar,
            strokeData = StrokeRepository.get(currentChar),
            completedStrokes = strokesForCurrentChar.toList(),
            nextStrokeIndex = strokesForCurrentChar.size
        )
    }

    private suspend fun saveStats() {
        val q = loadedQuestion
        if (q != null) {
            val allCorrect = charResults.isNotEmpty() && charResults.all { it }
            db.hiraganaQuestionDao().update(
                q.copy(
                    attemptCount = q.attemptCount + 1,
                    correctCount = q.correctCount + if (allCorrect) 1 else 0
                )
            )
        }
        val stats = db.lessonStatsDao().getBySubject(SubjectType.HIRAGANA.name)
            ?: LessonStats(subject = SubjectType.HIRAGANA.name)
        val newResults = AdaptiveDifficulty.appendResults(stats.recentResults, charResults)
        db.lessonStatsDao().upsert(
            stats.copy(
                recentResults = newResults,
                totalAttempts = stats.totalAttempts + charResults.size,
                totalCorrect = stats.totalCorrect + charResults.count { it }
            )
        )
    }
}
