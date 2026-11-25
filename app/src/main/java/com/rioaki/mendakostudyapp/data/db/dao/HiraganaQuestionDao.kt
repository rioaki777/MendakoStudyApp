package com.rioaki.mendakostudyapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rioaki.mendakostudyapp.data.db.entity.HiraganaQuestion

@Dao
interface HiraganaQuestionDao {

    @Query("SELECT * FROM hiragana_question ORDER BY attemptCount ASC, correctCount ASC LIMIT 1")
    suspend fun getNextQuestion(): HiraganaQuestion?

    @Query("SELECT * FROM hiragana_question WHERE id = :id")
    suspend fun getById(id: Int): HiraganaQuestion?

    /** 練習候補画面用。登録順（古い順）に並べる。 */
    @Query("SELECT * FROM hiragana_question ORDER BY id ASC")
    fun observeAllAsc(): LiveData<List<HiraganaQuestion>>

    @Query("SELECT * FROM hiragana_question ORDER BY id DESC")
    fun observeAll(): LiveData<List<HiraganaQuestion>>

    @Insert
    suspend fun insert(question: HiraganaQuestion)

    @Update
    suspend fun update(question: HiraganaQuestion)

    @Delete
    suspend fun delete(question: HiraganaQuestion)

    @Query("SELECT COUNT(*) FROM hiragana_question")
    suspend fun getCount(): Int
}
