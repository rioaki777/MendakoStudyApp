package com.rioaki.mendakostudyapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rioaki.mendakostudyapp.data.db.entity.FurniturePlacement

@Dao
interface FurniturePlacementDao {

    @Query("SELECT * FROM furniture_placement WHERE mendakoId = :mendakoId")
    fun observeForMendako(mendakoId: Int): LiveData<List<FurniturePlacement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(placement: FurniturePlacement)

    @Query("DELETE FROM furniture_placement WHERE mendakoId = :mendakoId AND itemId = :itemId")
    suspend fun delete(mendakoId: Int, itemId: Int)

    @Query("DELETE FROM furniture_placement")
    suspend fun deleteAll()
}
