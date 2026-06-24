package com.rioaki.mendakostudyapp.ui.mendako

import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.rioaki.mendakostudyapp.data.db.entity.FurniturePlacement
import com.rioaki.mendakostudyapp.data.db.entity.ShopItem

/**
 * 部屋の家具を比率座標(0〜1)で [FrameLayout] に描画する（表示のみ）。
 * 部屋画面・ごはん画面など、家具を眺めるだけの画面で共用する。
 * 移動/取り外しが必要なかぐ画面は独自にドラッグ処理を持つため対象外。
 */
object FurnitureRoomRenderer {

    private const val ICON_SIZE_DP = 112

    fun render(room: FrameLayout, placements: List<FurniturePlacement>, items: List<ShopItem>) {
        val context = room.context
        val iconSizePx = (ICON_SIZE_DP * context.resources.displayMetrics.density).toInt()
        val itemMap = items.associateBy { it.id }

        // 部屋サイズが必要なため計測後に描画する。
        room.post {
            val roomW = room.width.takeIf { it > 0 } ?: return@post
            val roomH = room.height.takeIf { it > 0 } ?: return@post
            room.removeAllViews()

            placements.forEach { placement ->
                val shopItem = itemMap[placement.itemId] ?: return@forEach
                val iv = ImageView(context).apply {
                    val resId = context.resources.getIdentifier(
                        shopItem.imageResName, "drawable", context.packageName
                    )
                    if (resId != 0) setImageResource(resId)
                    layoutParams = ViewGroup.LayoutParams(iconSizePx, iconSizePx)
                    x = (placement.x * (roomW - iconSizePx)).coerceIn(0f, (roomW - iconSizePx).toFloat())
                    y = (placement.y * (roomH - iconSizePx)).coerceIn(0f, (roomH - iconSizePx).toFloat())
                }
                room.addView(iv)
            }
        }
    }
}
