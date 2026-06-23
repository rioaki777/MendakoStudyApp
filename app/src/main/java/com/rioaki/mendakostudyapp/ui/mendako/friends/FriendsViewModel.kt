package com.rioaki.mendakostudyapp.ui.mendako.friends

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.rioaki.mendakostudyapp.data.db.AppDatabase
import com.rioaki.mendakostudyapp.data.model.MendakoCatalog
import com.rioaki.mendakostudyapp.data.model.MendakoDef
import kotlinx.coroutines.launch

class FriendsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val catalog: List<MendakoDef> = MendakoCatalog.all
    val currentPoints = db.userStateDao().observe().map { it?.currentPoints ?: 0 }
    val characterStates = db.mendakoCharacterStateDao().observeAll()

    val unlockResult = MutableLiveData<UnlockResult?>()

    sealed class UnlockResult {
        data class Success(val name: String) : UnlockResult()
        object NotEnoughPoints : UnlockResult()
    }

    fun unlock(def: MendakoDef) = viewModelScope.launch {
        val state = db.mendakoCharacterStateDao().getOnce(def.id)
        if (state?.unlocked == true) return@launch
        val points = db.userStateDao().getOnce()?.currentPoints ?: 0
        if (points < def.price) {
            unlockResult.postValue(UnlockResult.NotEnoughPoints)
            return@launch
        }
        db.userStateDao().subtractPoints(def.price)
        db.mendakoCharacterStateDao().setUnlocked(def.id)
        unlockResult.postValue(UnlockResult.Success(def.name))
    }

    fun clearUnlockResult() {
        unlockResult.value = null
    }
}
