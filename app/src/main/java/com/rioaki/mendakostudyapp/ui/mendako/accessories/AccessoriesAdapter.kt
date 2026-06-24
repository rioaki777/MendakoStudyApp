package com.rioaki.mendakostudyapp.ui.mendako.accessories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.data.db.entity.ShopItem
import com.rioaki.mendakostudyapp.databinding.ItemAccessoryCardBinding

class AccessoriesAdapter(
    private val onToggle: (itemId: Int, currentlyEquipped: Boolean) -> Unit
) : RecyclerView.Adapter<AccessoriesAdapter.ViewHolder>() {

    private var items: List<ShopItem> = emptyList()
    private var equippedIds: List<Int> = emptyList()

    fun update(items: List<ShopItem>, equippedIds: List<Int>) {
        this.items = items
        this.equippedIds = equippedIds
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAccessoryCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemAccessoryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShopItem) {
            val context = binding.root.context
            val resId = context.resources.getIdentifier(item.imageResName, "drawable", context.packageName)
            if (resId != 0) binding.ivAccessoryIcon.setImageResource(resId) else binding.ivAccessoryIcon.setImageDrawable(null)

            binding.tvAccessoryName.text = item.name

            val equipped = item.id in equippedIds
            binding.btnEquipToggle.text = context.getString(
                if (equipped) R.string.btn_unequip else R.string.btn_equip
            )
            binding.btnEquipToggle.setOnClickListener { onToggle(item.id, equipped) }
        }
    }
}
