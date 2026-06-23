package com.rioaki.mendakostudyapp.ui.mendako.accessories

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rioaki.mendakostudyapp.data.db.entity.MendakoCharacterState
import com.rioaki.mendakostudyapp.data.db.entity.ShopItem
import com.rioaki.mendakostudyapp.databinding.FragmentAccessoriesBinding
import com.rioaki.mendakostudyapp.ui.mendako.MendakoRenderer

class AccessoriesFragment : Fragment() {

    private var _binding: FragmentAccessoriesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AccessoriesViewModel by viewModels()

    private lateinit var adapter: AccessoriesAdapter
    private var allAccessoryItems: List<ShopItem> = emptyList()
    private var ownedIds: Set<Int> = emptySet()
    private var activeMendakoId = 0
    private var characterStates: List<MendakoCharacterState> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccessoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AccessoriesAdapter { itemId, _ -> viewModel.toggleEquip(itemId) }
        binding.rvAccessories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAccessories.adapter = adapter

        MendakoRenderer.tintAccessoryOverlays(
            binding.ivAccessoryHat, binding.ivAccessoryScarf, binding.ivAccessoryRibbon
        )

        setupDrag(binding.ivAccessoryHat, 4)
        setupDrag(binding.ivAccessoryScarf, 5)
        setupDrag(binding.ivAccessoryRibbon, 6)

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        viewModel.allItems.observe(viewLifecycleOwner) {
            allAccessoryItems = it
            refresh()
        }

        viewModel.ownedAccessories.observe(viewLifecycleOwner) {
            ownedIds = it.map { owned -> owned.itemId }.toSet()
            refresh()
        }

        viewModel.activeMendakoId.observe(viewLifecycleOwner) {
            activeMendakoId = it
            refresh()
        }

        viewModel.characterStates.observe(viewLifecycleOwner) {
            characterStates = it
            refresh()
        }
    }

    private fun equippedIds(): List<Int> = MendakoRenderer.parseEquipped(
        characterStates.firstOrNull { it.id == activeMendakoId }?.equippedAccessories
    )

    private fun positions(): Map<Int, Pair<Float, Float>> = MendakoRenderer.parsePositions(
        characterStates.firstOrNull { it.id == activeMendakoId }?.accessoryPositions
    )

    /** プレビュー上のアクセサリーを指でドラッグして位置を調整できるようにする。 */
    @SuppressLint("ClickableViewAccessibility")
    private fun setupDrag(view: ImageView, itemId: Int) {
        var downRawX = 0f
        var downRawY = 0f
        var startTranslationX = 0f
        var startTranslationY = 0f
        view.setOnTouchListener { v, event ->
            val container = binding.mendakoContainer
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX
                    downRawY = event.rawY
                    startTranslationX = v.translationX
                    startTranslationY = v.translationY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // コンテナ内に収まる範囲へクランプする。
                    v.translationX = (startTranslationX + (event.rawX - downRawX))
                        .coerceIn(-v.left.toFloat(), (container.width - v.right).toFloat())
                    v.translationY = (startTranslationY + (event.rawY - downRawY))
                        .coerceIn(-v.top.toFloat(), (container.height - v.bottom).toFloat())
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val w = container.width.takeIf { it > 0 } ?: 1
                    val h = container.height.takeIf { it > 0 } ?: 1
                    viewModel.updatePosition(itemId, v.translationX / w, v.translationY / h)
                    v.performClick()
                    true
                }
                else -> false
            }
        }
    }

    private fun refresh() {
        val equipped = equippedIds()
        MendakoRenderer.applyBody(binding.ivMendakoBody, activeMendakoId)
        MendakoRenderer.applyAccessories(
            binding.ivAccessoryHat, binding.ivAccessoryScarf, binding.ivAccessoryRibbon,
            equipped, positions()
        )

        val visible = allAccessoryItems.filter { it.id in ownedIds }
        if (visible.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvAccessories.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvAccessories.visibility = View.VISIBLE
            adapter.update(visible, equipped)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
