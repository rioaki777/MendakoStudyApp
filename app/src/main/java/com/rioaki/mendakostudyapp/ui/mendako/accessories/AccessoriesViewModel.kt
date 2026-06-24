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

    val ownedAccessories = db.ownedItemDao().observeByCategory("ACCESSORY")

    val allItems = db.shopItemDao().observeByCategory("ACCESSORY")

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

    /** 選択中個体のアクセサリー表示位置（コンテナに対するオフセット比率）を保存する。 */
    fun updatePosition(itemId: Int, fractionX: Float, fractionY: Float) = viewModelScope.launch {
        val activeId = db.userStateDao().getOnce()?.activeMendakoId ?: MendakoCatalog.DEFAULT_ID
        val state = db.mendakoCharacterStateDao().getOnce(activeId) ?: return@launch
        val positions = MendakoRenderer.parsePositions(state.accessoryPositions).toMutableMap()
        positions[itemId] = fractionX to fractionY
        db.mendakoCharacterStateDao().updatePositions(activeId, MendakoRenderer.positionsToJson(positions))
    }
}
