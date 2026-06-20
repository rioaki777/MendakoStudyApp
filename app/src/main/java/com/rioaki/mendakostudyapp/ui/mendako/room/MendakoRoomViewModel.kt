package com.rioaki.mendakostudyapp.ui.mendako.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import com.rioaki.mendakostudyapp.data.db.AppDatabase

class MendakoRoomViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val equippedIds = db.userStateDao().observe().map { state ->
        val json = state?.equippedAccessories ?: "[]"
        json.trim('[', ']').split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    val placements = db.furniturePlacementDao().observeAll()
    val allItems = db.shopItemDao().observeAll()
}
