package com.rioaki.mendakostudyapp.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.map
import com.rioaki.mendakostudyapp.data.db.AppDatabase

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    val currentPoints = db.userStateDao().observe().map { it?.currentPoints ?: 0 }
    val equippedAccessories = db.userStateDao().observe().map {
        val json = it?.equippedAccessories ?: "[]"
        json.trim('[', ']').split(",").mapNotNull { s -> s.trim().toIntOrNull() }
    }
}
