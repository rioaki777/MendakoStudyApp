package com.rioaki.mendakostudyapp.ui.lesson.hiragana

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.rioaki.mendakostudyapp.data.db.AppDatabase
import com.rioaki.mendakostudyapp.data.db.entity.HiraganaQuestion

/** ひらがな練習の候補画面。登録済みの問題一覧を提供する。 */
class HiraganaSelectViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val questions: LiveData<List<HiraganaQuestion>> = db.hiraganaQuestionDao().observeAllAsc()
}
