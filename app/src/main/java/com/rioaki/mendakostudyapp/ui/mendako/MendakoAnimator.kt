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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MendakoAnimator(
    private val container: View,
    private val ivEyes: ImageView,
    private val ivMouth: ImageView,
    lifecycleOwner: LifecycleOwner,
    private val ivFood: ImageView? = null,
    private val heartLayer: ViewGroup? = null
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var floatAnimator: ObjectAnimator? = null
    private var eatAnimator: ObjectAnimator? = null
    private var reactionJob: Job? = null
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
        val offsetPx = 20f * container.resources.displayMetrics.density
        floatAnimator = ObjectAnimator.ofFloat(container, "translationY", -offsetPx, offsetPx).apply {
            duration = 2000
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun stopFloating() {
        floatAnimator?.cancel()
        floatAnimator = null
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
