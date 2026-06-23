package com.rioaki.mendakostudyapp.ui.mendako.room

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.data.db.entity.MendakoCharacterState
import com.rioaki.mendakostudyapp.databinding.FragmentMendakoRoomBinding
import com.rioaki.mendakostudyapp.ui.mendako.MendakoRenderer

class MendakoRoomFragment : Fragment() {

    private var _binding: FragmentMendakoRoomBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MendakoRoomViewModel by viewModels()

    private var activeMendakoId = 0
    private var characterStates: List<MendakoCharacterState> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMendakoRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MendakoRenderer.tintAccessoryOverlays(
            binding.ivAccessoryHat, binding.ivAccessoryScarf, binding.ivAccessoryRibbon
        )

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
    }

    private fun renderMendako() {
        val binding = _binding ?: return
        MendakoRenderer.applyBody(binding.ivMendakoBody, activeMendakoId)
        val equipped = MendakoRenderer.parseEquipped(
            characterStates.firstOrNull { it.id == activeMendakoId }?.equippedAccessories
        )
        MendakoRenderer.applyAccessories(
            binding.ivAccessoryHat, binding.ivAccessoryScarf, binding.ivAccessoryRibbon, equipped
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
