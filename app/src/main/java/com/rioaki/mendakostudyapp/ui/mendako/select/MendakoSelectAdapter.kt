package com.rioaki.mendakostudyapp.ui.mendako.select

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rioaki.mendakostudyapp.data.model.MendakoDef
import com.rioaki.mendakostudyapp.databinding.ItemMendakoSelectCardBinding
import com.rioaki.mendakostudyapp.ui.mendako.MendakoRenderer

class MendakoSelectAdapter(
    private val onSelect: (MendakoDef) -> Unit
) : RecyclerView.Adapter<MendakoSelectAdapter.ViewHolder>() {

    private var items: List<MendakoDef> = emptyList()
    private var activeId: Int = 0

    fun update(items: List<MendakoDef>, activeId: Int) {
        this.items = items
        this.activeId = activeId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMendakoSelectCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemMendakoSelectCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(def: MendakoDef) {
            MendakoRenderer.applyBody(binding.ivMendakoBody, def.id)
            binding.tvMendakoName.text = def.name

            val selected = def.id == activeId
            binding.tvSelectedLabel.visibility = if (selected) View.VISIBLE else View.GONE
            binding.btnSelect.visibility = if (selected) View.GONE else View.VISIBLE
            binding.btnSelect.setOnClickListener { onSelect(def) }
        }
    }
}
