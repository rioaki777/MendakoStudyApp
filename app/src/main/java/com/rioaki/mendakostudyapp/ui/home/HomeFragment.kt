package com.rioaki.mendakostudyapp.ui.home

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.audio.AppAudioManager
import com.rioaki.mendakostudyapp.data.db.entity.FurniturePlacement
import com.rioaki.mendakostudyapp.data.db.entity.MendakoCharacterState
import com.rioaki.mendakostudyapp.data.db.entity.ShopItem
import com.rioaki.mendakostudyapp.databinding.FragmentHomeBinding
import com.rioaki.mendakostudyapp.ui.mendako.FurnitureRoomRenderer
import com.rioaki.mendakostudyapp.ui.mendako.MendakoAnimator
import com.rioaki.mendakostudyapp.ui.mendako.MendakoRenderer

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var mendakoAnimator: MendakoAnimator

    private var activeMendakoId = 0
    private var characterStates: List<MendakoCharacterState> = emptyList()
    private var placements: List<FurniturePlacement> = emptyList()
    private var allItems: List<ShopItem> = emptyList()

    private var versionTapCount = 0
    private val resetTapHandler = Handler(Looper.getMainLooper())
    private val resetTapRunnable = Runnable { versionTapCount = 0 }

    // メンダコ本体の連打検出（5秒以内に5回タップで tuntun を再生）
    private val mendakoTapTimestamps = ArrayDeque<Long>()

    // 泡（awa.png）の生成を一定間隔で繰り返すループ
    private val bubbleHandler = Handler(Looper.getMainLooper())
    private val bubbleRunnable = object : Runnable {
        override fun run() {
            spawnBubble()
            // 次の泡までの間隔をランダムに（ときどき複数同時に出てもよい）
            bubbleHandler.postDelayed(this, (500L..2200L).random())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mendakoAnimator = MendakoAnimator(
            container = binding.mendakoContainer,
            ivEyes = binding.ivMendakoEyes,
            ivMouth = binding.ivMendakoMouth,
            lifecycleOwner = viewLifecycleOwner,
            roamArea = true,
            topBoundView = binding.menuBar
        )

        viewModel.currentPoints.observe(viewLifecycleOwner) { points ->
            binding.tvPoints.text = getString(R.string.points_format, points)
        }

        viewModel.activeMendakoId.observe(viewLifecycleOwner) { id ->
            activeMendakoId = id
            renderMendako()
        }
        viewModel.characterStates.observe(viewLifecycleOwner) { states ->
            characterStates = states
            renderMendako()
        }
        viewModel.allItems.observe(viewLifecycleOwner) { items ->
            allItems = items
            renderFurniture()
        }
        viewModel.placements.observe(viewLifecycleOwner) { list ->
            placements = list
            renderFurniture()
        }

        binding.mendakoContainer.setOnClickListener {
            val now = System.currentTimeMillis()
            mendakoTapTimestamps.addLast(now)
            // 5秒より古いタップを捨てる
            while (mendakoTapTimestamps.isNotEmpty() && now - mendakoTapTimestamps.first() > 5000) {
                mendakoTapTimestamps.removeFirst()
            }
            if (mendakoTapTimestamps.size >= 5) {
                // 5秒以内に5回タップ → tuntun を再生してカウントをリセット
                mendakoTapTimestamps.clear()
                AppAudioManager.playSeAsset(requireContext(), "audio/se/tuntun.wav")
            } else {
                AppAudioManager.playSeAsset(requireContext(), "audio/se/nnn.wav")
            }
            mendakoAnimator.react(com.rioaki.mendakostudyapp.ui.mendako.MendakoState.HAPPY)
        }

        binding.btnStudy.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_lessonSelection)
        }
        binding.btnShop.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_shop)
        }
        binding.btnRoom.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_mendakoRoom)
        }
        binding.btnFriends.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_friends)
        }

        binding.tvVersion.setOnClickListener {
            resetTapHandler.removeCallbacks(resetTapRunnable)
            versionTapCount++
            if (versionTapCount >= 5) {
                versionTapCount = 0
                findNavController().navigate(R.id.action_home_to_adminPanel)
            } else {
                resetTapHandler.postDelayed(resetTapRunnable, 2000)
            }
        }

        // レイアウト確定後に泡の生成ループを開始する
        binding.bubbleLayer.post {
            if (_binding != null) bubbleHandler.post(bubbleRunnable)
        }
    }

    /** 画面最下部のランダムな位置から awa.png を1つ生成し、横揺れしながら上端へ移動させて消す。 */
    private fun spawnBubble() {
        val binding = _binding ?: return
        val layer = binding.bubbleLayer
        val w = layer.width
        val h = layer.height
        if (w <= 0 || h <= 0) return

        val density = resources.displayMetrics.density
        val sizePx = ((18..46).random() * density).toInt()
        val swayAmp = (8..24).random() * density

        val iv = ImageView(requireContext()).apply {
            setImageResource(R.drawable.awa)
            layoutParams = FrameLayout.LayoutParams(sizePx, sizePx)
            alpha = 0f
        }
        // 横位置（揺れ幅を考慮して画面内に収める）
        val centerX = (swayAmp.toInt()..(w - sizePx - swayAmp.toInt()).coerceAtLeast(swayAmp.toInt() + 1)).random().toFloat()
        iv.translationX = centerX - swayAmp
        iv.translationY = h.toFloat()
        layer.addView(iv)

        // 下端 → 上端の外側まで上昇
        val rise = ObjectAnimator.ofFloat(iv, View.TRANSLATION_Y, h.toFloat(), -sizePx.toFloat()).apply {
            duration = (4000L..8000L).random()
            interpolator = LinearInterpolator()
        }
        // 横揺れ（左右に往復）
        val sway = ObjectAnimator.ofFloat(iv, View.TRANSLATION_X, centerX - swayAmp, centerX + swayAmp).apply {
            duration = (900L..1600L).random()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }
        // フェードイン
        val fadeIn = ObjectAnimator.ofFloat(iv, View.ALPHA, 0f, 0.85f).apply { duration = 600 }

        rise.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                sway.cancel()
                (_binding?.bubbleLayer ?: layer).removeView(iv)
            }
        })

        sway.start()
        fadeIn.start()
        rise.start()
    }

    /** 選択中個体の部屋に置かれた家具をメンダコの背面に表示する（表示のみ）。 */
    private fun renderFurniture() {
        val binding = _binding ?: return
        FurnitureRoomRenderer.render(binding.flRoom, placements, allItems)
    }

    private fun renderMendako() {
        val binding = _binding ?: return
        MendakoRenderer.applyBody(binding.ivMendakoBody, activeMendakoId)
        val state = characterStates.firstOrNull { it.id == activeMendakoId }
        val equipped = MendakoRenderer.parseEquipped(state?.equippedAccessories)
        val positions = MendakoRenderer.parsePositions(state?.accessoryPositions)
        MendakoRenderer.applyAccessories(binding.mendakoContainer, equipped, positions)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resetTapHandler.removeCallbacks(resetTapRunnable)
        bubbleHandler.removeCallbacks(bubbleRunnable)
        _binding = null
    }
}
