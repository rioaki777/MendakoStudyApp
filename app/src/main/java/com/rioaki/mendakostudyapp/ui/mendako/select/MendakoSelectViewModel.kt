package com.rioaki.mendakostudyapp.ui.mendako.select

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.rioaki.mendakostudyapp.data.db.AppDatabase
import com.rioaki.mendakostudyapp.data.model.MendakoCatalog
import com.rioaki.mendakostudyapp.data.model.MendakoDef
import kotlinx.coroutines.launch

class MendakoSelectViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val activeMendakoId = db.userStateDao().observe().map { it?.activeMendakoId ?: MendakoCatalog.DEFAULT_ID }
    val characterStates = db.mendakoCharacterStateDao().observeAll()

    /** アンロック済みの個体のみをカタログ順で返す。 */
    fun unlockedDefs(unlockedIds: Set<Int>): List<MendakoDef> =
        MendakoCatalog.all.filter { it.id == MendakoCatalog.DEFAULT_ID || it.id in unlockedIds }

    fun select(id: Int) = viewModelScope.launch {
        db.userStateDao().updateActiveMendako(id)
    }
}
