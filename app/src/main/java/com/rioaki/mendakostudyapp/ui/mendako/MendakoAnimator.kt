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
    lifecycleOwner: LifecycleOwner
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var floatAnimator: ObjectAnimator? = null
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

    fun reset() {
        reactionJob?.cancel()
        setExpression(MendakoState.NORMAL)
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
    }

    private fun setExpression(state: MendakoState) {
        ivEyes.setImageResource(when (state) {
            MendakoState.NORMAL -> R.drawable.mendako_eyes_normal
            MendakoState.HAPPY  -> R.drawable.mendako_eyes_happy
            MendakoState.SAD    -> R.drawable.mendako_eyes_sad
        })
        ivMouth.setImageResource(when (state) {
            MendakoState.NORMAL -> R.drawable.mendako_mouth_normal
            MendakoState.HAPPY  -> R.drawable.mendako_mouth_happy
            MendakoState.SAD    -> R.drawable.mendako_mouth_sad
        })
    }
}
