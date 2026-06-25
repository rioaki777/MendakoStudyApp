package com.rioaki.mendakostudyapp.ui.mendako.furniture

import android.content.ClipData
import android.graphics.Color
import android.os.Bundle
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.data.db.entity.FurniturePlacement
import com.rioaki.mendakostudyapp.data.db.entity.ShopItem
import com.rioaki.mendakostudyapp.databinding.FragmentFurnitureBinding

class FurnitureFragment : Fragment() {

    private var _binding: FragmentFurnitureBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FurnitureViewModel by viewModels()

    private lateinit var listAdapter: FurnitureListAdapter
    private var allFurnitureItems: List<ShopItem> = emptyList()
    private var ownedIds: Set<Int> = emptySet()
    private var placements: List<FurniturePlacement> = emptyList()
    private var draggedView: View? = null
    private var dropHandled = false

    private val iconSizeDp = 112

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFurnitureBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listAdapter = FurnitureListAdapter(
            onPlace = { itemId -> viewModel.place(itemId) },
            onRemove = { itemId -> viewModel.removePlacement(itemId) }
        )
        binding.rvUnplaced.layoutManager =
            GridLayoutManager(requireContext(), 3)
        binding.rvUnplaced.adapter = listAdapter

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        setupRoomDropTarget()

        viewModel.allFurnitureItems.observe(viewLifecycleOwner) {
            allFurnitureItems = it
            refresh()
        }

        viewModel.ownedFurniture.observe(viewLifecycleOwner) { owned ->
            ownedIds = owned.map { it.itemId }.toSet()
            refresh()
        }

        viewModel.placements.observe(viewLifecycleOwner) {
            placements = it
            rebuildRoomViews()
            refresh()
        }
    }

    private fun refresh() {
        val ownedItems = allFurnitureItems.filter { it.id in ownedIds }
        val placedIds = placements.map { it.itemId }.toSet()

        if (ownedItems.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvUnplaced.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvUnplaced.visibility = View.VISIBLE
            listAdapter.update(ownedItems, placedIds)
        }
    }

    private fun rebuildRoomViews() {
        val density = resources.displayMetrics.density
        val iconSizePx = (iconSizeDp * density).toInt()

        val itemMap = allFurnitureItems.associateBy { it.id }

        binding.flRoom.post {
            val roomW = binding.flRoom.width.takeIf { it > 0 } ?: return@post
            val roomH = binding.flRoom.height.takeIf { it > 0 } ?: return@post

            // 削除と再追加を同一フレームで行い、家具が一瞬消えるちらつきを防ぐ。
            binding.flRoom.removeAllViews()

            placements.forEach { placement ->
                val shopItem = itemMap[placement.itemId] ?: return@forEach
                val iv = ImageView(requireContext()).apply {
                    val resId = resources.getIdentifier(shopItem.imageResName, "drawable", requireContext().packageName)
                    if (resId != 0) setImageResource(resId)
                    tag = placement.itemId
                    setBackgroundColor(Color.TRANSPARENT)

                    val params = ViewGroup.LayoutParams(iconSizePx, iconSizePx)
                    layoutParams = params
                    x = (placement.x * (roomW - iconSizePx)).coerceIn(0f, (roomW - iconSizePx).toFloat())
                    y = (placement.y * (roomH - iconSizePx)).coerceIn(0f, (roomH - iconSizePx).toFloat())

                    setOnLongClickListener { v ->
                        val itemId = v.tag as Int
                        val clip = ClipData.newPlainText("itemId", itemId.toString())
                        val shadow = View.DragShadowBuilder(v)
                        v.startDragAndDrop(clip, shadow, itemId, View.DRAG_FLAG_OPAQUE)
                        draggedView = v
                        v.visibility = View.INVISIBLE
                        true
                    }

                    setOnClickListener { v ->
                        val itemId = v.tag as Int
                        val name = allFurnitureItems.find { it.id == itemId }?.name ?: ""
                        AlertDialog.Builder(requireContext())
                            .setTitle("$name を はずしますか？")
                            .setPositiveButton(R.string.dialog_remove_furniture_yes) { _, _ ->
                                viewModel.removePlacement(itemId)
                            }
                            .setNegativeButton(R.string.dialog_remove_furniture_no, null)
                            .show()
                    }
                }
                binding.flRoom.addView(iv)
            }
        }
    }

    private fun setupRoomDropTarget() {
        val density = resources.displayMetrics.density
        val iconSizePx = (iconSizeDp * density).toInt()

        binding.flRoom.setOnDragListener { roomView, event ->
            when (event.action) {
                DragEvent.ACTION_DRAG_STARTED -> true
                DragEvent.ACTION_DRAG_ENTERED, DragEvent.ACTION_DRAG_LOCATION -> true
                DragEvent.ACTION_DROP -> {
                    val itemId = event.clipData.getItemAt(0).text.toString().toInt()
                    // 表示側(FurnitureRoomRenderer / rebuildRoomViews)は x = frac * (roomW - iconSizePx)
                    // で左上座標を求めるため、保存する比率も同じ「配置可能範囲(roomW - iconSizePx)」基準で計算する。
                    val rangeW = (roomView.width - iconSizePx).coerceAtLeast(1)
                    val rangeH = (roomView.height - iconSizePx).coerceAtLeast(1)
                    val xFrac = ((event.x - iconSizePx / 2f) / rangeW).coerceIn(0f, 1f)
                    val yFrac = ((event.y - iconSizePx / 2f) / rangeH).coerceIn(0f, 1f)
                    // 元の View を即座に移動先へ配置してから表示を戻す。
                    // DB更新→再描画は非同期で遅れるため、ここで先に動かさないと
                    // ACTION_DRAG_ENDED で元の位置に一瞬表示されてしまう。
                    draggedView?.let { v ->
                        v.x = xFrac * rangeW
                        v.y = yFrac * rangeH
                        v.visibility = View.VISIBLE
                    }
                    dropHandled = true
                    viewModel.updatePlacement(itemId, xFrac, yFrac)
                    true
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    // ドロップ済みなら上で表示を戻しているので何もしない。
                    // ドロップされなかった（範囲外でキャンセル）場合のみ元に戻す。
                    if (!dropHandled) {
                        draggedView?.visibility = View.VISIBLE
                    }
                    draggedView = null
                    dropHandled = false
                    true
                }
                else -> true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
