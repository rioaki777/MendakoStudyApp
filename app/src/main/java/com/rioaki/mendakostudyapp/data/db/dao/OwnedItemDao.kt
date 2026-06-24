package com.rioaki.mendakostudyapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rioaki.mendakostudyapp.data.db.entity.OwnedItem

@Dao
interface OwnedItemDao {

    @Query("SELECT * FROM owned_item")
    fun observeAll(): LiveData<List<OwnedItem>>

    @Query(
        "SELECT o.* FROM owned_item o " +
            "INNER JOIN shop_item s ON o.itemId = s.id " +
            "WHERE s.category = :category " +
            "ORDER BY s.id"
    )
    fun observeByCategory(category: String): LiveData<List<OwnedItem>>

    @Query("SELECT * FROM owned_item WHERE itemId = :itemId")
    suspend fun getById(itemId: Int): OwnedItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: OwnedItem)

    @Query("UPDATE owned_item SET quantity = quantity - 1 WHERE itemId = :itemId AND quantity > 0")
    suspend fun decrementQuantity(itemId: Int)
}
