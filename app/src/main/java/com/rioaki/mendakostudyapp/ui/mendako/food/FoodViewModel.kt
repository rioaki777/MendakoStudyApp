package com.rioaki.mendakostudyapp.ui.mendako.food

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.rioaki.mendakostudyapp.data.db.AppDatabase
import com.rioaki.mendakostudyapp.data.model.MendakoCatalog
import kotlinx.coroutines.launch

class FoodViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val ownedFood = db.ownedItemDao().observeAll().map { list ->
        list.filter { it.itemId in 1..3 && it.quantity > 0 }
    }

    val allFoodItems = db.shopItemDao().observeAll().map { list ->
        list.filter { it.id in 1..3 }
    }

    /** 家具描画用：全アイテム定義と、選択中個体の部屋の家具配置。 */
    val allItems = db.shopItemDao().observeAll()

    val activeMendakoId =
        db.userStateDao().observe().map { it?.activeMendakoId ?: MendakoCatalog.DEFAULT_ID }

    /** アクセサリー描画用：選択中個体の装備状態。 */
    val characterStates = db.mendakoCharacterStateDao().observeAll()

    val placements = activeMendakoId.switchMap { db.furniturePlacementDao().observeForMendako(it) }

    /** 給餌イベント。値は食べた食べ物の画像リソース名（解決できなければ空文字）。 */
    val feedEvent = MutableLiveData<String?>()

    fun feed(itemId: Int) = viewModelScope.launch {
        db.ownedItemDao().decrementQuantity(itemId)
        val imageResName = db.shopItemDao().getById(itemId)?.imageResName ?: ""
        feedEvent.postValue(imageResName)
    }

    fun clearFeedEvent() {
        feedEvent.value = null
    }
}
