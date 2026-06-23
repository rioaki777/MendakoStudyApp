package com.rioaki.mendakostudyapp.ui.admin

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.data.db.entity.HiraganaQuestion
import com.rioaki.mendakostudyapp.databinding.FragmentAdminPanelBinding
import com.rioaki.mendakostudyapp.databinding.ItemHiraganaQuestionBinding

class AdminPanelFragment : Fragment() {

    private var _binding: FragmentAdminPanelBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AdminPanelViewModel by viewModels()
    private val adapter = QuestionAdapter(onEdit = ::showEditDialog, onDelete = ::showDeleteDialog)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminPanelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvQuestions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvQuestions.adapter = adapter
        viewModel.questions.observe(viewLifecycleOwner) { adapter.submitList(it) }
        binding.fabAdd.setOnClickListener { showEditDialog(null) }
        binding.btnStrokeCapture.setOnClickListener {
            findNavController().navigate(R.id.action_admin_to_strokeCapture)
        }
        binding.btnEditPoints.setOnClickListener { showPointsDialog() }
    }

    private fun showPointsDialog() {
        val current = viewModel.currentPoints.value ?: 0
        val editText = EditText(requireContext()).apply {
            hint = "所持ポイント"
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(current.toString())
            setSelection(text.length)
        }
        val container = FrameLayout(requireContext()).apply {
            val dp24 = (24 * resources.displayMetrics.density).toInt()
            setPadding(dp24, dp24 / 2, dp24, 0)
            addView(editText)
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("所持ポイント変更")
            .setView(container)
            .setPositiveButton("保存", null)
            .setNegativeButton("キャンセル", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val points = editText.text.toString().trim().toIntOrNull()
            if (points == null || points < 0) {
                Toast.makeText(requireContext(), "0以上の数値を入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.setPoints(points)
            Toast.makeText(requireContext(), "所持ポイントを${points}に変更しました", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
    }

    private fun showEditDialog(question: HiraganaQuestion?) {
        val editText = EditText(requireContext()).apply {
            hint = "ひらがなで入力（例: いぬ）"
            setText(question?.text ?: "")
            setSelection(text.length)
        }
        val container = FrameLayout(requireContext()).apply {
            val dp24 = (24 * resources.displayMetrics.density).toInt()
            setPadding(dp24, dp24 / 2, dp24, 0)
            addView(editText)
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (question == null) "問題を追加" else "問題を編集")
            .setView(container)
            .setPositiveButton("保存", null)
            .setNegativeButton("キャンセル", null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val text = editText.text.toString().trim()
            val error = validate(text)
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val isDuplicate = adapter.currentList.any { it.id != (question?.id ?: -1) && it.text == text }
            if (isDuplicate) {
                Toast.makeText(requireContext(), "同じ問題文が既に存在します", Toast.LENGTH_SHORT).show()
            }
            if (question == null) viewModel.addQuestion(text) else viewModel.updateQuestion(question, text)
            dialog.dismiss()
        }
    }

    private fun showDeleteDialog(question: HiraganaQuestion) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("削除の確認")
            .setMessage("「${question.text}」を削除しますか？")
            .setPositiveButton("削除") { _, _ -> viewModel.deleteQuestion(question) }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun validate(text: String): String? = when {
        text.isEmpty() -> "1文字以上入力してください"
        text.length > 20 -> "20文字以内で入力してください"
        !Regex("^[ぁ-ゞ ]+$").matches(text) -> "ひらがなとスペースのみ使えます"
        else -> null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class QuestionAdapter(
        private val onEdit: (HiraganaQuestion) -> Unit,
        private val onDelete: (HiraganaQuestion) -> Unit
    ) : ListAdapter<HiraganaQuestion, QuestionAdapter.ViewHolder>(DIFF) {

        companion object {
            private val DIFF = object : DiffUtil.ItemCallback<HiraganaQuestion>() {
                override fun areItemsTheSame(a: HiraganaQuestion, b: HiraganaQuestion) = a.id == b.id
                override fun areContentsTheSame(a: HiraganaQuestion, b: HiraganaQuestion) = a == b
            }
        }

        inner class ViewHolder(private val binding: ItemHiraganaQuestionBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(q: HiraganaQuestion) {
                binding.tvQuestionText.text = q.text
                val rate = if (q.attemptCount > 0) q.correctCount * 100 / q.attemptCount else 0
                binding.tvStats.text = "受講${q.attemptCount}回　正解率${rate}%"
                binding.btnEdit.setOnClickListener { onEdit(q) }
                binding.btnDelete.setOnClickListener { onDelete(q) }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemHiraganaQuestionBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))
    }
}
