package com.rioaki.mendakostudyapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rioaki.mendakostudyapp.data.db.entity.UserState

@Dao
interface UserStateDao {

    @Query("SELECT * FROM user_state WHERE id = 1")
    fun observe(): LiveData<UserState?>

    @Query("SELECT * FROM user_state WHERE id = 1")
    suspend fun getOnce(): UserState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(userState: UserState)

    @Query("UPDATE user_state SET currentPoints = currentPoints + :amount WHERE id = 1")
    suspend fun addPoints(amount: Int)

    @Query("UPDATE user_state SET currentPoints = MAX(0, currentPoints - :amount) WHERE id = 1")
    suspend fun subtractPoints(amount: Int)

    @Query("UPDATE user_state SET currentPoints = MAX(0, :points) WHERE id = 1")
    suspend fun setPoints(points: Int)

    @Query("UPDATE user_state SET equippedAccessories = :accessories WHERE id = 1")
    suspend fun updateEquippedAccessories(accessories: String)

    @Query("UPDATE user_state SET activeMendakoId = :id WHERE id = 1")
    suspend fun updateActiveMendako(id: Int)
}
