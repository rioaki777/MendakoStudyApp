package com.rioaki.mendakostudyapp.ui.mendako.accessories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.rioaki.mendakostudyapp.data.db.AppDatabase
import kotlinx.coroutines.launch

class AccessoriesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val ownedAccessories = db.ownedItemDao().observeAll().map { list ->
        list.filter { it.itemId in 4..6 }
    }

    val allItems = db.shopItemDao().observeAll().map { list ->
        list.filter { it.id in 4..6 }
    }

    val equippedIds = db.userStateDao().observe().map { state ->
        parseEquipped(state?.equippedAccessories ?: "[]")
    }

    fun toggleEquip(itemId: Int) = viewModelScope.launch {
        val state = db.userStateDao().getOnce() ?: return@launch
        val equipped = parseEquipped(state.equippedAccessories).toMutableList()
        if (itemId in equipped) equipped.remove(itemId) else equipped.add(itemId)
        db.userStateDao().updateEquippedAccessories(toJson(equipped))
    }

    private fun parseEquipped(json: String): List<Int> =
        json.trim('[', ']').split(",").mapNotNull { it.trim().toIntOrNull() }

    private fun toJson(ids: List<Int>): String = "[${ids.joinToString(",")}]"
}
