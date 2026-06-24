package com.rioaki.mendakostudyapp.ui.mendako.room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.data.db.entity.FurniturePlacement
import com.rioaki.mendakostudyapp.data.db.entity.MendakoCharacterState
import com.rioaki.mendakostudyapp.data.db.entity.ShopItem
import com.rioaki.mendakostudyapp.databinding.FragmentMendakoRoomBinding
import com.rioaki.mendakostudyapp.ui.mendako.FurnitureRoomRenderer
import com.rioaki.mendakostudyapp.ui.mendako.MendakoRenderer

class MendakoRoomFragment : Fragment() {

    private var _binding: FragmentMendakoRoomBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MendakoRoomViewModel by viewModels()

    private var activeMendakoId = 0
    private var characterStates: List<MendakoCharacterState> = emptyList()
    private var placements: List<FurniturePlacement> = emptyList()
    private var allItems: List<ShopItem> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMendakoRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAccessories.setOnClickListener {
            findNavController().navigate(R.id.action_mendakoRoom_to_accessories)
        }
        binding.btnFood.setOnClickListener {
            findNavController().navigate(R.id.action_mendakoRoom_to_food)
        }
        binding.btnFurniture.setOnClickListener {
            findNavController().navigate(R.id.action_mendakoRoom_to_furniture)
        }
        binding.btnSelectMendako.setOnClickListener {
            findNavController().navigate(R.id.action_mendakoRoom_to_mendakoSelect)
        }
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
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
    }

    /** 選択中個体の部屋に置かれた家具を表示する（部屋画面では表示のみ。移動はかぐ画面）。 */
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
        _binding = null
    }
}
