package com.rioaki.mendakostudyapp.ui.mendako.accessories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.rioaki.mendakostudyapp.data.db.AppDatabase
import com.rioaki.mendakostudyapp.data.model.MendakoCatalog
import com.rioaki.mendakostudyapp.ui.mendako.MendakoRenderer
import kotlinx.coroutines.launch

class AccessoriesViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val ownedAccessories = db.ownedItemDao().observeAll().map { list ->
        list.filter { it.itemId in 4..6 }
    }

    val allItems = db.shopItemDao().observeAll().map { list ->
        list.filter { it.id in 4..6 }
    }

    val activeMendakoId = db.userStateDao().observe().map { it?.activeMendakoId ?: MendakoCatalog.DEFAULT_ID }
    val characterStates = db.mendakoCharacterStateDao().observeAll()

    /** 選択中個体の装備アクセサリーを着脱する。 */
    fun toggleEquip(itemId: Int) = viewModelScope.launch {
        val activeId = db.userStateDao().getOnce()?.activeMendakoId ?: MendakoCatalog.DEFAULT_ID
        val state = db.mendakoCharacterStateDao().getOnce(activeId) ?: return@launch
        val equipped = MendakoRenderer.parseEquipped(state.equippedAccessories).toMutableList()
        if (itemId in equipped) equipped.remove(itemId) else equipped.add(itemId)
        db.mendakoCharacterStateDao().updateEquipped(activeId, MendakoRenderer.toJson(equipped))
    }
}
