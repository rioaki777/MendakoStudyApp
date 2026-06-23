package com.rioaki.mendakostudyapp.ui.mendako.accessories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private fun refresh() {
        val equipped = equippedIds()
        MendakoRenderer.applyBody(binding.ivMendakoBody, activeMendakoId)
        MendakoRenderer.applyAccessories(
            binding.ivAccessoryHat, binding.ivAccessoryScarf, binding.ivAccessoryRibbon, equipped
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
