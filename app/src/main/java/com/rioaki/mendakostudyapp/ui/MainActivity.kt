package com.rioaki.mendakostudyapp.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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

        // 画面下部のナビゲーションバー（とステータスバー）を隠して全画面表示にする。
        // スワイプで一時的に出せる sticky モード。
        applyImmersiveMode()

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
                R.id.furnitureFragment,
                R.id.friendsFragment,
                R.id.mendakoSelectFragment -> AppAudioManager.BgmType.HOME_SHOP
                else -> AppAudioManager.BgmType.MAIN
            }
            AppAudioManager.playBgm(this, bgmType)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // フォーカス復帰時（ダイアログ閉鎖など）にもバーが出たままにならないよう再適用する。
        if (hasFocus) applyImmersiveMode()
    }

    private fun applyImmersiveMode() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, binding.root)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
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
