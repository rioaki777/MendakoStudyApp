package com.rioaki.mendakostudyapp.ui.mendako

import android.view.Gravity

/**
 * アクセサリー（ショップ category="ACCESSORY"）の見た目メタデータ。
 * メンダコ本体に重ねる際の既定サイズ(dp)・基準位置(gravity/余白)を定義する。
 * 本体への重ね描画は [MendakoRenderer.applyAccessories] がこの定義をもとに ImageView を動的生成する。
 * ここに id と画像名(drawable)・配置を追加すれば、レイアウトを触らずに新しいアクセサリーを増やせる。
 * dp サイズは基準コンテナ一辺 [MendakoRenderer.REFERENCE_SIZE_DP] を基準に比例スケールされる。
 */
object AccessoryCatalog {

    data class AccessoryDef(
        /** ShopItem.id と一致させる。 */
        val id: Int,
        val imageResName: String,
        val widthDp: Int,
        val heightDp: Int,
        val gravity: Int,
        val marginTopDp: Int = 0,
        val marginEndDp: Int = 0,
    )

    val all = listOf(
        AccessoryDef(4, "item_hat", 160, 96, Gravity.TOP or Gravity.CENTER_HORIZONTAL, marginTopDp = 4),
        AccessoryDef(5, "item_scarf", 240, 64, Gravity.CENTER),
        AccessoryDef(6, "item_ribbon", 96, 96, Gravity.TOP or Gravity.END, marginTopDp = 12, marginEndDp = 8),
        AccessoryDef(16, "item_glasses", 150, 80, Gravity.CENTER),
        AccessoryDef(17, "item_crown", 150, 96, Gravity.TOP or Gravity.CENTER_HORIZONTAL),
        AccessoryDef(18, "item_headphone", 170, 120, Gravity.TOP or Gravity.CENTER_HORIZONTAL),
    )

    fun byId(id: Int): AccessoryDef? = all.firstOrNull { it.id == id }
}
