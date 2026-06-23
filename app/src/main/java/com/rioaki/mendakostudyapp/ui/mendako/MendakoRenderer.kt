package com.rioaki.mendakostudyapp.ui.mendako

import android.graphics.Color
import android.widget.ImageView
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.data.model.MendakoCatalog
import com.rioaki.mendakostudyapp.data.model.MendakoDef

/**
 * ホーム/部屋/アクセサリー画面で共通する「選択中メンダコ個体の本体＋装備」描画を集約する。
 * 本体画像は [MendakoDef.imageResName] を解決して使い、未解決（友達の専用画像が未追加）の場合は
 * デフォルト本体に [MendakoDef.placeholderColor] の色フィルタをかけて区別する。
 */
object MendakoRenderer {

    // アクセサリーの着色（既存の各画面・AccessoriesAdapter と同じ配色）
    private const val COLOR_HAT = "#6A0DAD"
    private const val COLOR_SCARF = "#FF6B35"
    private const val COLOR_RIBBON = "#FF69B4"

    /** 一覧カード等で使うアクセサリ着色の初期化（本体の重ね表示用 ImageView 向け）。 */
    fun tintAccessoryOverlays(hat: ImageView, scarf: ImageView, ribbon: ImageView) {
        hat.setColorFilter(Color.parseColor(COLOR_HAT))
        scarf.setColorFilter(Color.parseColor(COLOR_SCARF))
        ribbon.setColorFilter(Color.parseColor(COLOR_RIBBON))
    }

    /** 本体 ImageView に、指定個体の見た目（実画像 or プレースホルダ色）を適用する。 */
    fun applyBody(body: ImageView, mendakoId: Int) {
        val def: MendakoDef = MendakoCatalog.byId(mendakoId)
        val context = body.context
        val resId = context.resources.getIdentifier(def.imageResName, "drawable", context.packageName)
        if (resId != 0) {
            body.setImageResource(resId)
        } else {
            body.setImageResource(R.drawable.mendako_body)
        }
        if (def.placeholderColor != 0 && resId == 0) {
            body.setColorFilter(def.placeholderColor)
        } else {
            body.clearColorFilter()
        }
    }

    /** 装備中アクセサリーIDに応じて重ね表示 ImageView の表示/非表示を切り替える。 */
    fun applyAccessories(
        hat: ImageView,
        scarf: ImageView,
        ribbon: ImageView,
        equippedIds: List<Int>
    ) {
        hat.visibility = if (4 in equippedIds) ImageView.VISIBLE else ImageView.GONE
        scarf.visibility = if (5 in equippedIds) ImageView.VISIBLE else ImageView.GONE
        ribbon.visibility = if (6 in equippedIds) ImageView.VISIBLE else ImageView.GONE
    }

    /** JSON文字列("[4,5]")を装備IDリストに変換する。 */
    fun parseEquipped(json: String?): List<Int> =
        (json ?: "[]").trim('[', ']').split(",").mapNotNull { it.trim().toIntOrNull() }

    /** 装備IDリストをJSON文字列に変換する。 */
    fun toJson(ids: List<Int>): String = "[${ids.joinToString(",")}]"
}
