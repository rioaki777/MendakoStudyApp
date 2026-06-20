package com.rioaki.mendakostudyapp.ui.lesson.selection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.databinding.FragmentLessonSelectionBinding

class LessonSelectionFragment : Fragment() {

    private var _binding: FragmentLessonSelectionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LessonSelectionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.additionAccuracy.observe(viewLifecycleOwner) { pct ->
            binding.tvAdditionAccuracy.text = getString(R.string.accuracy_format, pct)
        }
        viewModel.subtractionAccuracy.observe(viewLifecycleOwner) { pct ->
            binding.tvSubtractionAccuracy.text = getString(R.string.accuracy_format, pct)
        }
        viewModel.hiraganaAccuracy.observe(viewLifecycleOwner) { pct ->
            binding.tvHiraganaAccuracy.text = getString(R.string.accuracy_format, pct)
        }

        binding.cardAddition.setOnClickListener {
            findNavController().navigate(R.id.action_lessonSelection_to_addition)
        }
        binding.cardSubtraction.setOnClickListener {
            findNavController().navigate(R.id.action_lessonSelection_to_subtraction)
        }
        binding.cardHiragana.setOnClickListener {
            findNavController().navigate(R.id.action_lessonSelection_to_hiragana)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
