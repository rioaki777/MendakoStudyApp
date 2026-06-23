package com.rioaki.mendakostudyapp.ui.mendako.friends

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.data.model.MendakoCatalog
import com.rioaki.mendakostudyapp.data.model.MendakoDef
import com.rioaki.mendakostudyapp.databinding.ItemFriendCardBinding
import com.rioaki.mendakostudyapp.ui.mendako.MendakoRenderer

class FriendsAdapter(
    private val onUnlock: (MendakoDef) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.ViewHolder>() {

    private var items: List<MendakoDef> = emptyList()
    private var unlockedIds: Set<Int> = emptySet()

    fun update(items: List<MendakoDef>, unlockedIds: Set<Int>) {
        this.items = items
        this.unlockedIds = unlockedIds
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFriendCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemFriendCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(def: MendakoDef) {
            val context = binding.root.context
            MendakoRenderer.applyBody(binding.ivFriendBody, def.id)
            binding.tvFriendName.text = def.name

            val unlocked = def.id == MendakoCatalog.DEFAULT_ID || def.id in unlockedIds
            if (unlocked) {
                binding.tvFriendStatus.text = context.getString(R.string.label_unlocked)
                binding.btnUnlock.visibility = View.GONE
            } else {
                binding.tvFriendStatus.text = context.getString(R.string.points_format, def.price)
                binding.btnUnlock.visibility = View.VISIBLE
                binding.btnUnlock.text = context.getString(R.string.btn_unlock_format, def.price)
                binding.btnUnlock.setOnClickListener { onUnlock(def) }
            }
        }
    }
}
