package com.rioaki.mendakostudyapp.ui.mendako.food

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rioaki.mendakostudyapp.data.db.entity.OwnedItem
import com.rioaki.mendakostudyapp.data.db.entity.ShopItem
import com.rioaki.mendakostudyapp.databinding.ItemFoodCardBinding

class FoodAdapter(
    private val onFeed: (itemId: Int) -> Unit
) : RecyclerView.Adapter<FoodAdapter.ViewHolder>() {

    private var items: List<ShopItem> = emptyList()
    private var ownedMap: Map<Int, Int> = emptyMap()

    fun update(items: List<ShopItem>, owned: List<OwnedItem>) {
        this.items = items
        this.ownedMap = owned.associate { it.itemId to it.quantity }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFoodCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemFoodCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShopItem) {
            val context = binding.root.context
            val resId = context.resources.getIdentifier(item.imageResName, "drawable", context.packageName)
            if (resId != 0) binding.ivFoodIcon.setImageResource(resId) else binding.ivFoodIcon.setImageDrawable(null)

            binding.tvFoodName.text = item.name
            val qty = ownedMap[item.id] ?: 0
            binding.tvFoodQty.text = "×${qty}"
            binding.btnFeed.isEnabled = qty > 0
            binding.btnFeed.setOnClickListener { onFeed(item.id) }
        }
    }
}
