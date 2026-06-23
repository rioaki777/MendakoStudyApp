package com.rioaki.mendakostudyapp.ui.mendako

import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
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

class MendakoAnimator(
    private val container: View,
    private val ivEyes: ImageView,
    private val ivMouth: ImageView,
    lifecycleOwner: LifecycleOwner,
    private val ivFood: ImageView? = null
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var floatAnimator: ObjectAnimator? = null
    private var eatAnimator: ObjectAnimator? = null
    private var reactionJob: Job? = null

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
                setMouth(MendakoState.NORMAL)
                delay(munchMs)
                setMouth(MendakoState.HAPPY)
                delay(munchMs)
            }
            container.scaleY = 1f
            setExpression(MendakoState.NORMAL)
        }
    }

    fun reset() {
        reactionJob?.cancel()
        eatAnimator?.cancel()
        hideFood()
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
