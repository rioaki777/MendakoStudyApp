package com.rioaki.mendakostudyapp.ui.mendako.furniture

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rioaki.mendakostudyapp.data.db.entity.ShopItem
import com.rioaki.mendakostudyapp.databinding.ItemFurnitureCardBinding

class FurnitureListAdapter(
    private val onPlace: (itemId: Int) -> Unit
) : RecyclerView.Adapter<FurnitureListAdapter.ViewHolder>() {

    private var items: List<ShopItem> = emptyList()
    private var placedIds: Set<Int> = emptySet()

    fun update(items: List<ShopItem>, placedIds: Set<Int>) {
        this.items = items
        this.placedIds = placedIds
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFurnitureCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemFurnitureCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShopItem) {
            val context = binding.root.context
            val resId = context.resources.getIdentifier(item.imageResName, "drawable", context.packageName)
            if (resId != 0) binding.ivFurnitureIcon.setImageResource(resId) else binding.ivFurnitureIcon.setImageDrawable(null)
            binding.tvFurnitureName.text = item.name

            val placed = item.id in placedIds
            binding.btnPlace.isEnabled = !placed
            binding.btnPlace.text = if (placed) "はいち中" else context.getString(com.rioaki.mendakostudyapp.R.string.btn_place)
            binding.btnPlace.setOnClickListener { if (!placed) onPlace(item.id) }
        }
    }
}
