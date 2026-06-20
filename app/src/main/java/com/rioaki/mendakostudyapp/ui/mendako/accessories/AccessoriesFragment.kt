package com.rioaki.mendakostudyapp.ui.mendako.accessories

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rioaki.mendakostudyapp.data.db.entity.ShopItem
import com.rioaki.mendakostudyapp.databinding.FragmentAccessoriesBinding

class AccessoriesFragment : Fragment() {

    private var _binding: FragmentAccessoriesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AccessoriesViewModel by viewModels()

    private lateinit var adapter: AccessoriesAdapter
    private var allAccessoryItems: List<ShopItem> = emptyList()
    private var equippedIds: List<Int> = emptyList()
    private var ownedIds: Set<Int> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccessoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AccessoriesAdapter { itemId, equipped -> viewModel.toggleEquip(itemId) }
        binding.rvAccessories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAccessories.adapter = adapter

        binding.ivAccessoryHat.setColorFilter(Color.parseColor("#6A0DAD"))
        binding.ivAccessoryScarf.setColorFilter(Color.parseColor("#FF6B35"))
        binding.ivAccessoryRibbon.setColorFilter(Color.parseColor("#FF69B4"))

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        viewModel.allItems.observe(viewLifecycleOwner) {
            allAccessoryItems = it
            refresh()
        }

        viewModel.ownedAccessories.observe(viewLifecycleOwner) {
            ownedIds = it.map { owned -> owned.itemId }.toSet()
            refresh()
        }

        viewModel.equippedIds.observe(viewLifecycleOwner) {
            equippedIds = it
            updateAccessoryOverlay()
            refresh()
        }
    }

    private fun refresh() {
        val visible = allAccessoryItems.filter { it.id in ownedIds }
        if (visible.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvAccessories.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvAccessories.visibility = View.VISIBLE
            adapter.update(visible, equippedIds)
        }
    }

    private fun updateAccessoryOverlay() {
        binding.ivAccessoryHat.visibility = if (4 in equippedIds) View.VISIBLE else View.GONE
        binding.ivAccessoryScarf.visibility = if (5 in equippedIds) View.VISIBLE else View.GONE
        binding.ivAccessoryRibbon.visibility = if (6 in equippedIds) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
