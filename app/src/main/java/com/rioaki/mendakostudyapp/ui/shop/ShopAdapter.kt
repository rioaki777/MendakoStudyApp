package com.rioaki.mendakostudyapp.ui.shop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rioaki.mendakostudyapp.data.db.entity.ShopItem
import com.rioaki.mendakostudyapp.databinding.ItemShopCardBinding

class ShopAdapter(
    private val onBuy: (ShopItem) -> Unit
) : RecyclerView.Adapter<ShopAdapter.ViewHolder>() {

    private var items: List<ShopItem> = emptyList()
    private var ownedMap: Map<Int, Int> = emptyMap()
    private var currentPoints: Int = 0

    fun update(items: List<ShopItem>, ownedMap: Map<Int, Int>, points: Int) {
        this.items = items
        this.ownedMap = ownedMap
        this.currentPoints = points
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShopCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemShopCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShopItem) {
            val context = binding.root.context
            val resId = context.resources.getIdentifier(item.imageResName, "drawable", context.packageName)
            if (resId != 0) binding.ivItemIcon.setImageResource(resId) else binding.ivItemIcon.setImageDrawable(null)

            binding.tvItemName.text = item.name
            binding.tvItemPrice.text = "⭐ ${item.price}pt"

            val owned = ownedMap[item.id] ?: 0
            if (owned > 0) {
                binding.tvOwnedCount.text = "もちもの: ${owned}こ"
                binding.tvOwnedCount.visibility = View.VISIBLE
            } else {
                binding.tvOwnedCount.visibility = View.GONE
            }

            binding.btnBuy.isEnabled = currentPoints >= item.price
            binding.btnBuy.setOnClickListener { onBuy(item) }
        }
    }
}
