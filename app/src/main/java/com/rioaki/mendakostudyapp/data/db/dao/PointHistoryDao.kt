package com.rioaki.mendakostudyapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import com.rioaki.mendakostudyapp.data.db.entity.PointHistory

@Dao
interface PointHistoryDao {

    @Insert
    suspend fun insert(history: PointHistory)
}
