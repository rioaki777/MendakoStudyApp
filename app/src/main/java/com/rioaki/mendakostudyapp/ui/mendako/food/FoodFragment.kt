package com.rioaki.mendakostudyapp.ui.mendako.food

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rioaki.mendakostudyapp.data.db.entity.FurniturePlacement
import com.rioaki.mendakostudyapp.data.db.entity.MendakoCharacterState
import com.rioaki.mendakostudyapp.data.db.entity.ShopItem
import com.rioaki.mendakostudyapp.databinding.FragmentFoodBinding
import com.rioaki.mendakostudyapp.ui.mendako.FurnitureRoomRenderer
import com.rioaki.mendakostudyapp.ui.mendako.MendakoAnimator
import com.rioaki.mendakostudyapp.ui.mendako.MendakoRenderer

class FoodFragment : Fragment() {

    private var _binding: FragmentFoodBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FoodViewModel by viewModels()

    private lateinit var adapter: FoodAdapter
    private lateinit var mendakoAnimator: MendakoAnimator
    private var allFoodItems: List<ShopItem> = emptyList()
    private var placements: List<FurniturePlacement> = emptyList()
    private var allItems: List<ShopItem> = emptyList()
    private var activeMendakoId = 0
    private var characterStates: List<MendakoCharacterState> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mendakoAnimator = MendakoAnimator(
            container = binding.mendakoContainer,
            ivEyes = binding.ivMendakoEyes,
            ivMouth = binding.ivMendakoMouth,
            lifecycleOwner = viewLifecycleOwner,
            ivFood = binding.ivFood
        )

        adapter = FoodAdapter { itemId -> viewModel.feed(itemId) }
        binding.rvFood.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFood.adapter = adapter

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        viewModel.allFoodItems.observe(viewLifecycleOwner) {
            allFoodItems = it
            refresh()
        }

        viewModel.ownedFood.observe(viewLifecycleOwner) { owned ->
            if (owned.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.rvFood.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.rvFood.visibility = View.VISIBLE
                adapter.update(allFoodItems, owned)
            }
        }

        viewModel.feedEvent.observe(viewLifecycleOwner) { imageResName ->
            imageResName ?: return@observe
            val foodResId = if (imageResName.isNotEmpty()) {
                resources.getIdentifier(imageResName, "drawable", requireContext().packageName)
            } else 0
            mendakoAnimator.eat(foodResId)
            viewModel.clearFeedEvent()
        }

        viewModel.allItems.observe(viewLifecycleOwner) { items ->
            allItems = items
            renderFurniture()
        }
        viewModel.placements.observe(viewLifecycleOwner) { list ->
            placements = list
            renderFurniture()
        }

        viewModel.activeMendakoId.observe(viewLifecycleOwner) { id ->
            activeMendakoId = id
            renderAccessories()
        }
        viewModel.characterStates.observe(viewLifecycleOwner) { states ->
            characterStates = states
            renderAccessories()
        }
    }

    /** 選択中個体の部屋に置かれた家具を表示する（ごはん画面では表示のみ）。 */
    private fun renderFurniture() {
        val binding = _binding ?: return
        FurnitureRoomRenderer.render(binding.flRoom, placements, allItems)
    }

    /** 選択中個体が装備中のアクセサリーを本体に重ねて表示する（ごはん画面では表示のみ）。 */
    private fun renderAccessories() {
        val binding = _binding ?: return
        val state = characterStates.firstOrNull { it.id == activeMendakoId }
        val equipped = MendakoRenderer.parseEquipped(state?.equippedAccessories)
        val positions = MendakoRenderer.parsePositions(state?.accessoryPositions)
        MendakoRenderer.applyAccessories(binding.mendakoContainer, equipped, positions)
    }

    private fun refresh() {
        val owned = viewModel.ownedFood.value ?: emptyList()
        if (owned.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvFood.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvFood.visibility = View.VISIBLE
            adapter.update(allFoodItems, owned)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
