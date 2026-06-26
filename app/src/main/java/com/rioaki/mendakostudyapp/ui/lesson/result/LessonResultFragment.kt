package com.rioaki.mendakostudyapp.ui.lesson.result

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.data.model.SubjectType
import com.rioaki.mendakostudyapp.databinding.FragmentLessonResultBinding
import com.rioaki.mendakostudyapp.ui.lesson.hiragana.DrawnGlyphView
import com.rioaki.mendakostudyapp.ui.lesson.hiragana.HiraganaResultHolder

class LessonResultFragment : Fragment() {

    private var _binding: FragmentLessonResultBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LessonResultViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLessonResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val subjectType = arguments?.getString("subjectType") ?: SubjectType.ADDITION.name
        val correctCount = arguments?.getInt("correctCount", 0) ?: 0
        val totalCount = arguments?.getInt("totalCount", 5) ?: 5
        val earnedPoints = arguments?.getInt("earnedPoints", 0) ?: 0

        binding.tvResult.text = getString(R.string.result_correct_format, totalCount, correctCount)
        binding.tvEarnedPoints.text = getString(R.string.result_points_format, earnedPoints)

        // ひらがな受講時は、メンダコの代わりにユーザーが書いた文字を横並び表示する
        if (subjectType == SubjectType.HIRAGANA.name && HiraganaResultHolder.lastResult.isNotEmpty()) {
            showDrawnChars()
        }

        viewModel.totalPoints.observe(viewLifecycleOwner) { total ->
            binding.tvTotalPoints.text = getString(R.string.result_total_points_format, total)
        }

        viewModel.awardPoints(earnedPoints, subjectType)

        binding.btnRetry.setOnClickListener {
            when (subjectType) {
                SubjectType.ADDITION.name ->
                    findNavController().navigate(R.id.action_result_retry_addition)
                SubjectType.SUBTRACTION.name ->
                    findNavController().navigate(R.id.action_result_retry_subtraction)
                SubjectType.HIRAGANA.name ->
                    findNavController().navigate(R.id.action_result_retry_hiragana)
            }
        }

        binding.btnOtherLesson.setOnClickListener {
            findNavController().popBackStack(R.id.lessonSelectionFragment, false)
        }

        binding.btnGoHome.setOnClickListener {
            findNavController().popBackStack(R.id.homeFragment, false)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack(R.id.lessonSelectionFragment, false)
        }
    }

    private fun showDrawnChars() {
        binding.ivMendakoReaction.visibility = View.GONE
        binding.hsvDrawnChars.visibility = View.VISIBLE

        val density = resources.displayMetrics.density
        val sizePx = (96 * density).toInt()
        val marginPx = (6 * density).toInt()

        binding.llDrawnChars.removeAllViews()
        for (drawn in HiraganaResultHolder.lastResult) {
            val glyph = DrawnGlyphView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(sizePx, sizePx).also {
                    it.setMargins(marginPx, 0, marginPx, 0)
                }
                setBackgroundColor(Color.WHITE)
                setGlyph(drawn.char, drawn.strokes)
            }
            binding.llDrawnChars.addView(glyph)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
