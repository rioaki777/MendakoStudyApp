package com.rioaki.mendakostudyapp.ui.mendako

import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.audio.AppAudioManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.random.Random

class MendakoAnimator(
    private val container: View,
    private val ivEyes: ImageView,
    private val ivMouth: ImageView,
    lifecycleOwner: LifecycleOwner,
    private val ivFood: ImageView? = null,
    private val heartLayer: ViewGroup? = null,
    // 画面いっぱいを自由に泳ぎ回らせるモード（ホーム用）。
    // true のとき縦揺れは行わず、[topBoundView] の下〜画面下端の範囲を回遊する。
    private val roamArea: Boolean = false,
    // 回遊の上限。このビュー（上部メニュー等）の下端より上には行かない。
    private val topBoundView: View? = null
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var floatAnimator: ObjectAnimator? = null
    private var eatAnimator: ObjectAnimator? = null
    private var reactionJob: Job? = null
    private var swimJob: Job? = null
    private val activeHearts = mutableListOf<View>()

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onResume(owner: LifecycleOwner) = startFloating()

    override fun onPause(owner: LifecycleOwner) = stopFloating()

    override fun onDestroy(owner: LifecycleOwner) = scope.cancel()

    fun react(state: MendakoState) {
        reactionJob?.cancel()
        setExpression(state)
        reactionJob = scope.launch {
            delay(1500)
            setExpression(MendakoState.NORMAL)
        }
    }

    /**
     * ごはんを食べているモーション。口元に食べ物（[foodResId]）を表示し、
     * 本体を「もぐもぐ」と縦に潰しながら、食べ物が口元で縮小して消える。
     * 最後にNORMALへ戻る。[foodResId] が 0 の場合は食べ物を表示しない。
     */
    fun eat(foodResId: Int) {
        reactionJob?.cancel()
        eatAnimator?.cancel()
        setExpression(MendakoState.HAPPY)

        val munchCount = 4
        val munchMs = 180L
        val totalMs = munchCount * 2 * munchMs

        eatAnimator = ObjectAnimator.ofFloat(container, "scaleY", 1f, 0.82f).apply {
            duration = munchMs
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = munchCount * 2 - 1
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        ivFood?.takeIf { foodResId != 0 }?.let { food ->
            food.setImageResource(foodResId)
            food.scaleX = 1f
            food.scaleY = 1f
            food.alpha = 1f
            food.visibility = View.VISIBLE
            food.animate()
                .scaleX(0.2f).scaleY(0.2f).alpha(0f)
                .setDuration(totalMs)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { food.visibility = View.GONE }
                .start()
        }

        reactionJob = scope.launch {
            repeat(munchCount) {
                ivMouth.setImageResource(R.drawable.mendako_mouth_eat_close)
                delay(munchMs)
                ivMouth.setImageResource(R.drawable.mendako_mouth_eat_open)
                delay(munchMs)
            }
            container.scaleY = 1f
            setExpression(MendakoState.NORMAL)
            emitHearts()
        }
    }

    /**
     * メンダコの周りにハートを舞わせる演出。ごはんを食べ終わった直後に呼ぶ。
     * [heartLayer]（メンダコ本体の親 FrameLayout）にハートの ImageView を動的生成し、
     * ふわっと上方へ広がりながらフェードアウトさせて、終了後に取り除く。
     */
    fun emitHearts() {
        val layer = heartLayer ?: return
        AppAudioManager.playSeAsset(layer.context, "audio/se/yammy.wav")
        val density = layer.resources.displayMetrics.density
        val sizePx = (34f * density).toInt()
        val centerX = container.x + container.width / 2f
        val baseY = container.y + container.height * 0.4f
        val count = 6
        repeat(count) { i ->
            val heart = ImageView(layer.context).apply {
                setImageResource(R.drawable.ic_heart)
                layoutParams = FrameLayout.LayoutParams(sizePx, sizePx)
                x = centerX - sizePx / 2f + (Random.nextFloat() - 0.5f) * container.width * 0.4f
                y = baseY - sizePx / 2f
                scaleX = 0.5f
                scaleY = 0.5f
                alpha = 0f
            }
            layer.addView(heart)
            activeHearts.add(heart)

            val driftX = (Random.nextFloat() - 0.5f) * 140f * density
            val riseY = (130f + Random.nextFloat() * 90f) * density
            heart.animate()
                .setStartDelay(i * 90L)
                .alpha(1f)
                .scaleX(1.1f).scaleY(1.1f)
                .translationXBy(driftX)
                .translationYBy(-riseY)
                .setDuration(1100L)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    heart.animate()
                        .alpha(0f)
                        .setDuration(250L)
                        .withEndAction {
                            layer.removeView(heart)
                            activeHearts.remove(heart)
                        }
                        .start()
                }
                .start()
        }
    }

    private fun clearHearts() {
        activeHearts.forEach {
            it.animate().cancel()
            (it.parent as? ViewGroup)?.removeView(it)
        }
        activeHearts.clear()
    }

    fun reset() {
        reactionJob?.cancel()
        eatAnimator?.cancel()
        hideFood()
        clearHearts()
        container.scaleY = 1f
        setExpression(MendakoState.NORMAL)
    }

    private fun hideFood() {
        ivFood?.let {
            it.animate().cancel()
            it.visibility = View.GONE
        }
    }

    private fun startFloating() {
        if (roamArea) {
            startRoaming()
            return
        }
        val offsetPx = 20f * container.resources.displayMetrics.density
        floatAnimator = ObjectAnimator.ofFloat(container, "translationY", -offsetPx, offsetPx).apply {
            duration = 2000
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        startSwimming()
    }

    /**
     * 縦揺れに加えて、時々ふわっと横へ泳ぐ演出。
     * ランダムな待機をはさみ、左右どちらかへゆっくり移動してから中央へ戻る。
     * [translationX] は縦揺れ（[translationY]）と別プロパティなので併走できる。
     */
    private fun startSwimming() {
        swimJob?.cancel()
        swimJob = scope.launch {
            val density = container.resources.displayMetrics.density
            while (true) {
                // 次に泳ぎ出すまでの待機（4〜9秒のランダム）
                delay(Random.nextLong(4000, 9000))
                val swimPx = (30f + Random.nextFloat() * 30f) * density
                val target = if (Random.nextBoolean()) swimPx else -swimPx
                val legMs = 1200L
                // 横へ移動 → 中央へ戻る
                container.animate()
                    .translationX(target)
                    .setDuration(legMs)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
                delay(legMs)
                container.animate()
                    .translationX(0f)
                    .setDuration(legMs)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
                delay(legMs)
            }
        }
    }

    /**
     * 画面いっぱい（上部メニューの下〜画面下端）をランダムに泳ぎ回るモード。
     * 回遊可能な矩形内のランダムな点へ、距離に応じた時間でゆっくり移動するのを繰り返す。
     * 進行方向に応じて本体を左右反転（[scaleX] の符号）させ、泳いでいる向きを表現する。
     */
    private fun startRoaming() {
        swimJob?.cancel()
        swimJob = scope.launch {
            val density = container.resources.displayMetrics.density
            val parent = container.parent as? View ?: return@launch
            // レイアウト確定（幅が入る）まで待つ
            while (container.width == 0 || parent.width == 0) delay(50)

            while (true) {
                val visHalfW = container.width * kotlin.math.abs(container.scaleX) / 2f
                val visHalfH = container.height * kotlin.math.abs(container.scaleY) / 2f
                val centerX = container.left + container.width / 2f
                val centerY = container.top + container.height / 2f

                val topBound = (topBoundView?.bottom ?: parent.paddingTop).toFloat() +
                    8f * density + visHalfH
                val bottomBound = parent.height - parent.paddingBottom - visHalfH
                val leftBound = parent.paddingLeft + visHalfW
                val rightBound = parent.width - parent.paddingRight - visHalfW

                // 中心が取りうる範囲 → translation 範囲へ変換
                val txMin = leftBound - centerX
                val txMax = rightBound - centerX
                val tyMin = topBound - centerY
                val tyMax = bottomBound - centerY

                val tx = if (txMax > txMin) txMin + Random.nextFloat() * (txMax - txMin) else 0f
                val ty = if (tyMax > tyMin) tyMin + Random.nextFloat() * (tyMax - tyMin) else 0f

                val dx = tx - container.translationX
                val dy = ty - container.translationY

                // 速度を一定に保つため、移動距離から所要時間を決める（約120dp/秒）
                val dist = hypot(dx, dy)
                val duration = (dist / (0.12f * density)).toLong().coerceIn(1500L, 4500L)

                container.animate()
                    .translationX(tx)
                    .translationY(ty)
                    .setDuration(duration)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
                delay(duration)
                // 次の移動までひと呼吸おく
                delay(Random.nextLong(500, 2000))
            }
        }
    }

    private fun stopFloating() {
        floatAnimator?.cancel()
        floatAnimator = null
        swimJob?.cancel()
        swimJob = null
        container.animate().cancel()
        container.translationX = 0f
        container.translationY = 0f
        eatAnimator?.cancel()
        eatAnimator = null
        hideFood()
        clearHearts()
        container.scaleY = 1f
    }

    private fun setExpression(state: MendakoState) {
        ivEyes.setImageResource(when (state) {
            MendakoState.NORMAL -> R.drawable.mendako_eyes_normal
            MendakoState.HAPPY  -> R.drawable.mendako_eyes_happy
            MendakoState.SAD    -> R.drawable.mendako_eyes_sad
        })
        setMouth(state)
    }

    private fun setMouth(state: MendakoState) {
        ivMouth.setImageResource(when (state) {
            MendakoState.NORMAL -> R.drawable.mendako_mouth_normal
            MendakoState.HAPPY  -> R.drawable.mendako_mouth_happy
            MendakoState.SAD    -> R.drawable.mendako_mouth_sad
        })
    }
}
