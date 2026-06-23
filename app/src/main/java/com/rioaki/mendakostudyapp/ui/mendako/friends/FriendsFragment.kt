package com.rioaki.mendakostudyapp.ui.mendako.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.databinding.FragmentFriendsBinding

class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FriendsViewModel by viewModels()

    private lateinit var adapter: FriendsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FriendsAdapter { def -> viewModel.unlock(def) }
        binding.rvFriends.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFriends.adapter = adapter

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        viewModel.currentPoints.observe(viewLifecycleOwner) { points ->
            binding.tvPoints.text = getString(R.string.points_format, points)
        }

        viewModel.characterStates.observe(viewLifecycleOwner) { states ->
            val unlockedIds = states.filter { it.unlocked }.map { it.id }.toSet()
            adapter.update(viewModel.catalog, unlockedIds)
        }

        viewModel.unlockResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is FriendsViewModel.UnlockResult.Success ->
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.msg_unlocked_format, result.name),
                        Toast.LENGTH_SHORT
                    ).show()
                FriendsViewModel.UnlockResult.NotEnoughPoints ->
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.msg_not_enough_points),
                        Toast.LENGTH_SHORT
                    ).show()
                null -> return@observe
            }
            viewModel.clearUnlockResult()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
