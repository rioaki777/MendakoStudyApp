package com.rioaki.mendakostudyapp.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.databinding.FragmentHomeBinding
import com.rioaki.mendakostudyapp.ui.mendako.MendakoAnimator

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var mendakoAnimator: MendakoAnimator

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

        binding.ivAccessoryHat.setColorFilter(Color.parseColor("#6A0DAD"))
        binding.ivAccessoryScarf.setColorFilter(Color.parseColor("#FF6B35"))
        binding.ivAccessoryRibbon.setColorFilter(Color.parseColor("#FF69B4"))

        viewModel.equippedAccessories.observe(viewLifecycleOwner) { equipped ->
            binding.ivAccessoryHat.visibility = if (4 in equipped) View.VISIBLE else View.GONE
            binding.ivAccessoryScarf.visibility = if (5 in equipped) View.VISIBLE else View.GONE
            binding.ivAccessoryRibbon.visibility = if (6 in equipped) View.VISIBLE else View.GONE
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
