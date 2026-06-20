package com.rioaki.mendakostudyapp.ui.mendako.room

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.databinding.FragmentMendakoRoomBinding

class MendakoRoomFragment : Fragment() {

    private var _binding: FragmentMendakoRoomBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MendakoRoomViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMendakoRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivAccessoryHat.setColorFilter(Color.parseColor("#6A0DAD"))
        binding.ivAccessoryScarf.setColorFilter(Color.parseColor("#FF6B35"))
        binding.ivAccessoryRibbon.setColorFilter(Color.parseColor("#FF69B4"))

        binding.btnAccessories.setOnClickListener {
            findNavController().navigate(R.id.action_mendakoRoom_to_accessories)
        }
        binding.btnFood.setOnClickListener {
            findNavController().navigate(R.id.action_mendakoRoom_to_food)
        }
        binding.btnFurniture.setOnClickListener {
            findNavController().navigate(R.id.action_mendakoRoom_to_furniture)
        }
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.equippedIds.observe(viewLifecycleOwner) { equipped ->
            binding.ivAccessoryHat.visibility = if (4 in equipped) View.VISIBLE else View.GONE
            binding.ivAccessoryScarf.visibility = if (5 in equipped) View.VISIBLE else View.GONE
            binding.ivAccessoryRibbon.visibility = if (6 in equipped) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
