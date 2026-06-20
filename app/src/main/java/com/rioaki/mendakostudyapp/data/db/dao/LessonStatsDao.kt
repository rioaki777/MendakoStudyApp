package com.rioaki.mendakostudyapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rioaki.mendakostudyapp.data.db.entity.LessonStats

@Dao
interface LessonStatsDao {

    @Query("SELECT * FROM lesson_stats WHERE subject = :subject")
    fun observe(subject: String): LiveData<LessonStats?>

    @Query("SELECT * FROM lesson_stats WHERE subject = :subject")
    suspend fun getBySubject(subject: String): LessonStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stats: LessonStats)
}
