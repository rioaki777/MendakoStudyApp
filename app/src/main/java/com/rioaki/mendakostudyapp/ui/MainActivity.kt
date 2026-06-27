package com.rioaki.mendakostudyapp.ui

import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
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

    /** スプラッシュ表示中の遷移を一意にするためのトークン。 */
    private var splashShowing = false

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

    override fun onStart() {
        super.onStart()
        // 初回起動・バックグラウンドからの復帰のたびにスプラッシュを表示する。
        showSplash()
    }

    /** mendako_study 画像をふわっと前面に表示し、少し遅らせて効果音を鳴らしてからフェードアウトする。 */
    private fun showSplash() {
        if (splashShowing) return
        splashShowing = true

        // スプラッシュ中は BGM を止め、効果音（mendako_study）だけを聞かせる。
        AppAudioManager.pauseBgm()

        val splash = binding.ivSplash
        splash.animate().cancel()
        splash.visibility = View.VISIBLE
        splash.alpha = 0f
        splash.scaleX = 0.9f
        splash.scaleY = 0.9f
        splash.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(FADE_IN_DURATION_MS)
            .setInterpolator(DecelerateInterpolator())
            .start()

        // 画像がふわっと出たあと、少し遅らせて効果音を再生。
        splash.postDelayed({
            AppAudioManager.playSeAsset(this, "audio/se/mendako_study.wav")
        }, SE_DELAY_MS)

        // 表示・効果音の余韻のあとにフェードアウトしてホーム（背面）を見せる。
        splash.postDelayed({ hideSplash() }, SPLASH_DURATION_MS)
    }

    private fun hideSplash() {
        val splash = binding.ivSplash
        splash.animate()
            .alpha(0f)
            .setDuration(FADE_OUT_DURATION_MS)
            .withEndAction {
                splash.visibility = View.GONE
                splashShowing = false
                // スプラッシュ終了後に BGM を再開する。
                AppAudioManager.resumeBgm()
            }
            .start()
    }

    override fun onResume() {
        super.onResume()
        // スプラッシュ表示中は BGM を再開しない（SE だけを聞かせるため）。
        if (!splashShowing) AppAudioManager.resumeBgm()
    }

    override fun onPause() {
        super.onPause()
        AppAudioManager.pauseBgm()
    }

    companion object {
        private const val FADE_IN_DURATION_MS = 700L
        private const val SE_DELAY_MS = 800L
        private const val SPLASH_DURATION_MS = 3600L
        private const val FADE_OUT_DURATION_MS = 400L
    }
}
