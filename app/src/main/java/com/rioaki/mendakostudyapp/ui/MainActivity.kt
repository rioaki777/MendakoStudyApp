package com.rioaki.mendakostudyapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.audio.AppAudioManager
import com.rioaki.mendakostudyapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController.addOnDestinationChangedListener { _, destination, _ ->
            val bgmType = when (destination.id) {
                R.id.additionLessonFragment,
                R.id.subtractionLessonFragment,
                R.id.hiraganaLessonFragment -> AppAudioManager.BgmType.STUDY
                R.id.shopFragment,
                R.id.mendakoRoomFragment,
                R.id.accessoriesFragment,
                R.id.foodFragment,
                R.id.furnitureFragment -> AppAudioManager.BgmType.HOME_SHOP
                else -> AppAudioManager.BgmType.MAIN
            }
            AppAudioManager.playBgm(this, bgmType)
        }
    }

    override fun onResume() {
        super.onResume()
        AppAudioManager.resumeBgm()
    }

    override fun onPause() {
        super.onPause()
        AppAudioManager.pauseBgm()
    }
}
