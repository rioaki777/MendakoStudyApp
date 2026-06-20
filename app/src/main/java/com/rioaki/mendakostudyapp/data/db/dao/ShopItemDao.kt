package com.rioaki.mendakostudyapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rioaki.mendakostudyapp.data.db.entity.ShopItem

@Dao
interface ShopItemDao {

    @Query("SELECT * FROM shop_item")
    fun observeAll(): LiveData<List<ShopItem>>

    @Query("SELECT * FROM shop_item WHERE category = :category")
    fun observeByCategory(category: String): LiveData<List<ShopItem>>

    @Query("SELECT * FROM shop_item WHERE id = :id")
    suspend fun getById(id: Int): ShopItem?

    @Query("SELECT COUNT(*) FROM shop_item")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<ShopItem>)
}
