package com.rioaki.mendakostudyapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rioaki.mendakostudyapp.data.db.entity.MendakoCharacterState

@Dao
interface MendakoCharacterStateDao {

    @Query("SELECT * FROM mendako_character")
    fun observeAll(): LiveData<List<MendakoCharacterState>>

    @Query("SELECT * FROM mendako_character WHERE id = :id")
    suspend fun getOnce(id: Int): MendakoCharacterState?

    @Query("SELECT COUNT(*) FROM mendako_character")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(state: MendakoCharacterState)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: MendakoCharacterState)

    @Query("UPDATE mendako_character SET unlocked = 1 WHERE id = :id")
    suspend fun setUnlocked(id: Int)

    @Query("UPDATE mendako_character SET equippedAccessories = :json WHERE id = :id")
    suspend fun updateEquipped(id: Int, json: String)

    @Query("UPDATE mendako_character SET accessoryPositions = :json WHERE id = :id")
    suspend fun updatePositions(id: Int, json: String)
}
