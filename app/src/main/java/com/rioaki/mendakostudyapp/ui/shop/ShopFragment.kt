package com.rioaki.mendakostudyapp.ui.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.data.db.entity.ShopItem
import com.rioaki.mendakostudyapp.databinding.FragmentShopBinding

class ShopFragment : Fragment() {

    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ShopViewModel by viewModels()

    private lateinit var adapter: ShopAdapter
    private var allItems: List<ShopItem> = emptyList()
    private val categories = listOf("FOOD", "ACCESSORY", "FURNITURE")
    private var selectedCategory = "FOOD"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ShopAdapter { item -> viewModel.purchase(item) }
        binding.rvShopItems.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvShopItems.adapter = adapter

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedCategory = categories[tab.position]
                refreshAdapter()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        viewModel.currentPoints.observe(viewLifecycleOwner) {
            binding.tvPoints.text = getString(R.string.points_format, it)
            refreshAdapter()
        }

        viewModel.allItems.observe(viewLifecycleOwner) {
            allItems = it
            refreshAdapter()
        }

        viewModel.ownedItems.observe(viewLifecycleOwner) { refreshAdapter() }

        viewModel.purchaseResult.observe(viewLifecycleOwner) { result ->
            result ?: return@observe
            val msg = when (result) {
                is ShopViewModel.PurchaseResult.Success -> "かえたよ！"
                is ShopViewModel.PurchaseResult.NotEnoughPoints -> "ポイントがたりないよ"
            }
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            viewModel.clearPurchaseResult()
        }
    }

    private fun refreshAdapter() {
        val filtered = allItems.filter { it.category == selectedCategory }
        val ownedMap = viewModel.ownedItems.value?.associate { it.itemId to it.quantity } ?: emptyMap()
        val points = viewModel.currentPoints.value ?: 0
        adapter.update(filtered, ownedMap, points)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
