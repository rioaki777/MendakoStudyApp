package com.rioaki.mendakostudyapp.ui.lesson.hiragana

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.data.db.entity.HiraganaQuestion
import com.rioaki.mendakostudyapp.databinding.ItemHiraganaSelectCardBinding

/**
 * 練習候補の一覧。先頭に「おまかせ」カードを置き、その後に登録済みの問題を並べる。
 * タップされたら問題の id（おまかせは [HiraganaSelectFragment.RANDOM_QUESTION_ID]）を返す。
 */
class HiraganaSelectAdapter(
    private val onSelect: (questionId: Int) -> Unit
) : RecyclerView.Adapter<HiraganaSelectAdapter.ViewHolder>() {

    private var items: List<HiraganaQuestion> = emptyList()

    fun update(items: List<HiraganaQuestion>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHiraganaSelectCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position == 0) holder.bindRandom() else holder.bind(items[position - 1])
    }

    // 先頭の「おまかせ」分 +1。問題が0件ならおまかせも出さない。
    override fun getItemCount() = if (items.isEmpty()) 0 else items.size + 1

    inner class ViewHolder(private val binding: ItemHiraganaSelectCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindRandom() {
            val ctx = binding.root.context
            binding.cardRoot.setCardBackgroundColor(Color.parseColor("#FFE082"))
            binding.tvQuestionText.text = ctx.getString(R.string.hiragana_random)
            binding.tvAttempt.visibility = View.GONE
            binding.cardRoot.setOnClickListener { onSelect(HiraganaSelectFragment.RANDOM_QUESTION_ID) }
        }

        fun bind(q: HiraganaQuestion) {
            val ctx = binding.root.context
            binding.cardRoot.setCardBackgroundColor(Color.WHITE)
            // 空白区切りの問題はレッスン画面と同じく改行して表示する
            binding.tvQuestionText.text = q.text.replace(' ', '\n')
            binding.tvAttempt.visibility = View.VISIBLE
            binding.tvAttempt.text = if (q.attemptCount > 0) {
                ctx.getString(R.string.hiragana_attempt_format, q.attemptCount)
            } else {
                ctx.getString(R.string.hiragana_never_tried)
            }
            binding.cardRoot.setOnClickListener { onSelect(q.id) }
        }
    }
}
