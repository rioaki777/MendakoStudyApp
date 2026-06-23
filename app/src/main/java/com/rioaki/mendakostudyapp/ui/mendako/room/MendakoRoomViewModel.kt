package com.rioaki.mendakostudyapp.ui.mendako.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import com.rioaki.mendakostudyapp.data.db.AppDatabase
import com.rioaki.mendakostudyapp.data.model.MendakoCatalog

class MendakoRoomViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val activeMendakoId = db.userStateDao().observe().map { it?.activeMendakoId ?: MendakoCatalog.DEFAULT_ID }
    val characterStates = db.mendakoCharacterStateDao().observeAll()

    val placements = db.furniturePlacementDao().observeAll()
    val allItems = db.shopItemDao().observeAll()
}
