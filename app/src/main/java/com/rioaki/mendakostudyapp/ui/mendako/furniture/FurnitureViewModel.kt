package com.rioaki.mendakostudyapp.ui.mendako.furniture

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.rioaki.mendakostudyapp.data.db.AppDatabase
import com.rioaki.mendakostudyapp.data.db.entity.FurniturePlacement
import com.rioaki.mendakostudyapp.data.model.MendakoCatalog
import kotlinx.coroutines.launch

class FurnitureViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val ownedFurniture = db.ownedItemDao().observeAll().map { list ->
        list.filter { it.itemId in 7..9 && it.quantity > 0 }
    }

    val allFurnitureItems = db.shopItemDao().observeAll().map { list ->
        list.filter { it.id in 7..9 }
    }

    private val activeMendakoId =
        db.userStateDao().observe().map { it?.activeMendakoId ?: MendakoCatalog.DEFAULT_ID }

    /** 選択中個体の部屋の家具配置のみを流す。 */
    val placements = activeMendakoId.switchMap { db.furniturePlacementDao().observeForMendako(it) }

    private suspend fun activeId(): Int =
        db.userStateDao().getOnce()?.activeMendakoId ?: MendakoCatalog.DEFAULT_ID

    fun place(itemId: Int) = viewModelScope.launch {
        db.furniturePlacementDao().upsert(FurniturePlacement(activeId(), itemId, 0.5f, 0.5f))
    }

    fun updatePlacement(itemId: Int, x: Float, y: Float) = viewModelScope.launch {
        db.furniturePlacementDao().upsert(FurniturePlacement(activeId(), itemId, x, y))
    }

    fun removePlacement(itemId: Int) = viewModelScope.launch {
        db.furniturePlacementDao().delete(activeId(), itemId)
    }
}
