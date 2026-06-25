package com.rioaki.mendakostudyapp.ui.lesson.hiragana

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.audio.AppAudioManager
import com.rioaki.mendakostudyapp.data.model.SubjectType
import com.rioaki.mendakostudyapp.databinding.FragmentHiraganaLessonBinding

class HiraganaLessonFragment : Fragment() {

    private var _binding: FragmentHiraganaLessonBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HiraganaLessonViewModel by viewModels()

    private var questionText: String = ""
    private var currentCharIdx: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHiraganaLessonBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.canvasHiragana.onStrokeCompleted = { stroke ->
            val canvas = binding.canvasHiragana
            viewModel.onStrokeCompleted(stroke, canvas.width, canvas.height)
        }

        viewModel.displayText.observe(viewLifecycleOwner) { text ->
            questionText = text
            updateProblemText()
        }

        viewModel.currentCharIndex.observe(viewLifecycleOwner) { idx ->
            currentCharIdx = idx
            updateProblemText()
            updateProgress()
        }

        viewModel.totalCharCount.observe(viewLifecycleOwner) { total ->
            if (total > 0) updateProgress()
        }

        viewModel.canvasState.observe(viewLifecycleOwner) { state ->
            state ?: return@observe
            binding.canvasHiragana.setGuideChar(state.guideChar, state.strokeData, state.nextStrokeIndex)
            binding.canvasHiragana.setCompletedStrokes(state.completedStrokes, state.nextStrokeIndex)
        }

        viewModel.strokeFeedback.observe(viewLifecycleOwner) { feedback ->
            when (feedback) {
                is StrokeFeedback.Wrong -> {
                    AppAudioManager.playStudyFail(requireContext())
                    binding.canvasHiragana.clearPendingStroke()
                }
                is StrokeFeedback.Correct -> AppAudioManager.playStudyPass(requireContext())
                else -> Unit
            }
        }

        viewModel.charComplete.observe(viewLifecycleOwner) { complete ->
            complete ?: return@observe
            flashCanvas(Color.parseColor("#A5D6A7"))
        }

        viewModel.lessonComplete.observe(viewLifecycleOwner) { complete ->
            complete ?: return@observe
            val bundle = Bundle().apply {
                putString("subjectType", SubjectType.HIRAGANA.name)
                putInt("correctCount", complete.correctCount)
                putInt("totalCount", complete.totalCount)
                putInt("earnedPoints", complete.earnedPoints)
            }
            findNavController().navigate(R.id.action_hiragana_to_result, bundle)
        }

        binding.btnRetryChar.setOnClickListener {
            binding.canvasHiragana.resetCanvas()
            viewModel.onRetry()
        }

        binding.btnDebugStrokes.alpha = 0.4f
        binding.btnDebugStrokes.setOnClickListener {
            val canvas = binding.canvasHiragana
            canvas.isDebugMode = !canvas.isDebugMode
            binding.btnDebugStrokes.alpha = if (canvas.isDebugMode) 1.0f else 0.4f
        }

        binding.btnQuit.setOnClickListener { showQuitDialog() }
    }

    private fun updateProgress() {
        val total = viewModel.totalCharCount.value ?: 0
        if (total > 0) {
            binding.tvProgress.text = getString(
                R.string.question_count_format, currentCharIdx + 1, total
            )
        }
    }

    private fun updateProblemText() {
        if (questionText.isEmpty()) return
        val spannable = SpannableString(questionText)
        val pos = findCharPositionInDisplayText(questionText, currentCharIdx)
        if (pos >= 0 && pos < questionText.length) {
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor("#E53935")),
                pos, pos + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                RelativeSizeSpan(1.3f),
                pos, pos + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        binding.tvProblem.text = spannable
    }

    private fun findCharPositionInDisplayText(displayText: String, charIdx: Int): Int {
        var count = 0
        for (i in displayText.indices) {
            val ch = displayText[i]
            if (ch != '\n') {
                if (count == charIdx) return i
                count++
            }
        }
        return -1
    }

    private fun flashCanvas(color: Int) {
        val canvas = binding.canvasHiragana
        ValueAnimator.ofObject(ArgbEvaluator(), color, Color.WHITE).apply {
            duration = 600
            addUpdateListener { animator ->
                canvas.setBackgroundColor(animator.animatedValue as Int)
            }
            start()
        }
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
