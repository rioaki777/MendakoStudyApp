package com.rioaki.mendakostudyapp.ui.lesson.hiragana

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.databinding.FragmentHiraganaSelectBinding

/** ひらがな練習の候補画面。どの問題を練習するかを選んでレッスンへ進む。 */
class HiraganaSelectFragment : Fragment() {

    private var _binding: FragmentHiraganaSelectBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HiraganaSelectViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHiraganaSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = HiraganaSelectAdapter { questionId -> startLesson(questionId) }
        binding.rvQuestions.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvQuestions.adapter = adapter

        viewModel.questions.observe(viewLifecycleOwner) { list ->
            adapter.update(list)
            binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun startLesson(questionId: Int) {
        val nav = findNavController()
        // 連打による二重遷移を防ぐ
        if (nav.currentDestination?.id != R.id.hiraganaSelectFragment) return
        nav.navigate(
            R.id.action_hiraganaSelect_to_hiragana,
            Bundle().apply { putInt(HiraganaLessonFragment.ARG_QUESTION_ID, questionId) }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /** 「おまかせ」を表す id。練習回数の少ない問題が自動で選ばれる。 */
        const val RANDOM_QUESTION_ID = -1
    }
}
