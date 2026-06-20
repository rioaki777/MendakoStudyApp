package com.rioaki.mendakostudyapp.ui.shop

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.rioaki.mendakostudyapp.data.db.AppDatabase
import com.rioaki.mendakostudyapp.data.db.entity.OwnedItem
import com.rioaki.mendakostudyapp.data.db.entity.ShopItem
import kotlinx.coroutines.launch

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val currentPoints = db.userStateDao().observe().map { it?.currentPoints ?: 0 }
    val allItems = db.shopItemDao().observeAll()
    val ownedItems = db.ownedItemDao().observeAll()

    val purchaseResult = MutableLiveData<PurchaseResult?>()

    sealed class PurchaseResult {
        object Success : PurchaseResult()
        object NotEnoughPoints : PurchaseResult()
    }

    fun purchase(item: ShopItem) = viewModelScope.launch {
        val points = db.userStateDao().getOnce()?.currentPoints ?: 0
        if (points < item.price) {
            purchaseResult.postValue(PurchaseResult.NotEnoughPoints)
            return@launch
        }
        db.userStateDao().subtractPoints(item.price)
        val existing = db.ownedItemDao().getById(item.id)
        db.ownedItemDao().upsert(OwnedItem(item.id, (existing?.quantity ?: 0) + 1))
        purchaseResult.postValue(PurchaseResult.Success)
    }

    fun clearPurchaseResult() {
        purchaseResult.value = null
    }
}
