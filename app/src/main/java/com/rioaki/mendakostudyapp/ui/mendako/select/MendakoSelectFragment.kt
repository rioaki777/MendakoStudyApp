package com.rioaki.mendakostudyapp.ui.mendako.select

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.rioaki.mendakostudyapp.databinding.FragmentMendakoSelectBinding

class MendakoSelectFragment : Fragment() {

    private var _binding: FragmentMendakoSelectBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MendakoSelectViewModel by viewModels()

    private lateinit var adapter: MendakoSelectAdapter

    private var unlockedIds: Set<Int> = emptySet()
    private var activeId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMendakoSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MendakoSelectAdapter { def ->
            viewModel.select(def.id)
            findNavController().popBackStack()
        }
        binding.rvMendako.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMendako.adapter = adapter

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        viewModel.characterStates.observe(viewLifecycleOwner) { states ->
            unlockedIds = states.filter { it.unlocked }.map { it.id }.toSet()
            refresh()
        }
        viewModel.activeMendakoId.observe(viewLifecycleOwner) { id ->
            activeId = id
            refresh()
        }
    }

    private fun refresh() {
        adapter.update(viewModel.unlockedDefs(unlockedIds), activeId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
