package com.rioaki.mendakostudyapp.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
            lifecycleOwner = viewLifecycleOwner
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
            AppAudioManager.playSeAsset(requireContext(), "audio/se/nnn.wav")
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
        _binding = null
    }
}
