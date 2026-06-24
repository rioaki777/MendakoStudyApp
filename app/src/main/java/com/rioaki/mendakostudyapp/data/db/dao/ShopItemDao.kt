package com.rioaki.mendakostudyapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rioaki.mendakostudyapp.data.db.entity.ShopItem

@Dao
interface ShopItemDao {

    @Query("SELECT * FROM shop_item ORDER BY id")
    fun observeAll(): LiveData<List<ShopItem>>

    @Query("SELECT * FROM shop_item WHERE category = :category ORDER BY id")
    fun observeByCategory(category: String): LiveData<List<ShopItem>>

    @Query("SELECT * FROM shop_item WHERE id = :id")
    suspend fun getById(id: Int): ShopItem?

    @Query("SELECT COUNT(*) FROM shop_item")
    suspend fun getCount(): Int

    /**
     * アイテム定義を投入/更新する。既存行（同一id）は最新の定義へ上書きする（自己修復）。
     * 旧バージョンで古い imageResName 等がシードされた端末でも、起動時に正しい定義へ揃える。
     */
    @Upsert
    suspend fun upsertAll(items: List<ShopItem>)
}
