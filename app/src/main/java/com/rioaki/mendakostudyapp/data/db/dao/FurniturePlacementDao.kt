package com.rioaki.mendakostudyapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rioaki.mendakostudyapp.data.db.entity.FurniturePlacement

@Dao
interface FurniturePlacementDao {

    @Query("SELECT * FROM furniture_placement")
    fun observeAll(): LiveData<List<FurniturePlacement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(placement: FurniturePlacement)

    @Delete
    suspend fun delete(placement: FurniturePlacement)

    @Query("DELETE FROM furniture_placement")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(placements: List<FurniturePlacement>)
}
