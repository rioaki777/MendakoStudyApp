package com.rioaki.mendakostudyapp.ui.lesson.addition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.data.model.SubjectType
import com.rioaki.mendakostudyapp.databinding.FragmentAdditionLessonBinding
import com.rioaki.mendakostudyapp.ui.mendako.MendakoAnimator
import com.rioaki.mendakostudyapp.ui.mendako.MendakoState

class AdditionLessonFragment : Fragment() {

    private var _binding: FragmentAdditionLessonBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdditionLessonViewModel by viewModels()

    private lateinit var mendakoAnimator: MendakoAnimator

    private val choiceButtons: List<MaterialButton> by lazy {
        listOf(binding.btnChoice0, binding.btnChoice1, binding.btnChoice2, binding.btnChoice3)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdditionLessonBinding.inflate(inflater, container, false)
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

        viewModel.currentQuestion.observe(viewLifecycleOwner) { question ->
            question ?: return@observe
            mendakoAnimator.reset()
            binding.tvQuestion.text = getString(
                R.string.question_addition_format, question.operandA, question.operandB
            )
            choiceButtons.forEachIndexed { i, btn ->
                btn.text = question.choices[i].toString()
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), com.google.android.material.R.color.design_default_color_primary))
                btn.isEnabled = true
            }
        }

        viewModel.currentIndex.observe(viewLifecycleOwner) { index ->
            binding.tvProgress.text = getString(
                R.string.question_count_format, index + 1, AdditionLessonViewModel.QUESTION_COUNT
            )
        }

        viewModel.answerFeedback.observe(viewLifecycleOwner) { feedback ->
            feedback ?: return@observe
            val question = viewModel.currentQuestion.value ?: return@observe
            mendakoAnimator.react(if (feedback.correct) MendakoState.HAPPY else MendakoState.SAD)
            choiceButtons.forEach { it.isEnabled = false }
            choiceButtons.forEachIndexed { i, btn ->
                val value = question.choices[i]
                when {
                    value == feedback.correctAnswer ->
                        btn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
                    i == feedback.selectedIndex && !feedback.correct ->
                        btn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
                }
            }
        }

        viewModel.lessonComplete.observe(viewLifecycleOwner) { complete ->
            complete ?: return@observe
            val bundle = Bundle().apply {
                putString("subjectType", SubjectType.ADDITION.name)
                putInt("correctCount", complete.correctCount)
                putInt("totalCount", complete.totalCount)
                putInt("earnedPoints", complete.earnedPoints)
            }
            findNavController().navigate(R.id.action_addition_to_result, bundle)
        }

        choiceButtons.forEachIndexed { index, btn ->
            btn.setOnClickListener { viewModel.submitAnswer(index) }
        }

        binding.btnQuit.setOnClickListener { showQuitDialog() }
    }

    private fun showQuitDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.quit_dialog_title)
            .setMessage(R.string.quit_dialog_message)
            .setPositiveButton(R.string.dialog_quit) { _, _ ->
                findNavController().popBackStack(R.id.homeFragment, false)
            }
            .setNegativeButton(R.string.dialog_continue, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
