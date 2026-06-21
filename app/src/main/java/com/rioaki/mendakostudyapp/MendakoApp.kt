package com.rioaki.mendakostudyapp

import android.app.Application
import com.rioaki.mendakostudyapp.audio.AppAudioManager
import com.rioaki.mendakostudyapp.data.db.AppDatabase
import com.rioaki.mendakostudyapp.data.seed.SeedData
import com.rioaki.mendakostudyapp.data.stroke.StrokeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MendakoApp : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        AppAudioManager.init(this)
        CoroutineScope(Dispatchers.IO).launch {
            SeedData.seed(database)
            StrokeRepository.load(assets)
        }
    }
}
