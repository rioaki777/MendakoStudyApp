package com.rioaki.mendakostudyapp.ui.mendako

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.data.model.MendakoCatalog
import com.rioaki.mendakostudyapp.data.model.MendakoDef
import org.json.JSONArray
import org.json.JSONObject

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

    /**
     * 装備中アクセサリーIDに応じて重ね表示 ImageView の表示/非表示を切り替え、
     * [positions]（コンテナサイズに対するオフセット比率）があれば既定位置からの translation を適用する。
     */
    fun applyAccessories(
        hat: ImageView,
        scarf: ImageView,
        ribbon: ImageView,
        equippedIds: List<Int>,
        positions: Map<Int, Pair<Float, Float>> = emptyMap()
    ) {
        applyAccessory(hat, 4, equippedIds, positions)
        applyAccessory(scarf, 5, equippedIds, positions)
        applyAccessory(ribbon, 6, equippedIds, positions)
    }

    private fun applyAccessory(
        view: ImageView,
        id: Int,
        equippedIds: List<Int>,
        positions: Map<Int, Pair<Float, Float>>
    ) {
        view.visibility = if (id in equippedIds) ImageView.VISIBLE else ImageView.GONE
        val pos = positions[id]
        val parent = view.parent as? View
        // 計測前でも固定サイズ(300dp)の layoutParams を基準に使えるようフォールバックする。
        val basisW = parent?.let { if (it.width > 0) it.width else it.layoutParams?.width ?: 0 } ?: 0
        val basisH = parent?.let { if (it.height > 0) it.height else it.layoutParams?.height ?: 0 } ?: 0
        view.translationX = (pos?.first ?: 0f) * basisW
        view.translationY = (pos?.second ?: 0f) * basisH
    }

    /** JSON文字列("[4,5]")を装備IDリストに変換する。 */
    fun parseEquipped(json: String?): List<Int> =
        (json ?: "[]").trim('[', ']').split(",").mapNotNull { it.trim().toIntOrNull() }

    /** 装備IDリストをJSON文字列に変換する。 */
    fun toJson(ids: List<Int>): String = "[${ids.joinToString(",")}]"

    /** 位置JSON(`{"4":[fx,fy]}`)を {アクセサリーID -> (fx, fy)} に変換する。 */
    fun parsePositions(json: String?): Map<Int, Pair<Float, Float>> {
        if (json.isNullOrBlank()) return emptyMap()
        return try {
            val obj = JSONObject(json)
            buildMap {
                for (key in obj.keys()) {
                    val id = key.toIntOrNull() ?: continue
                    val arr = obj.optJSONArray(key) ?: continue
                    if (arr.length() >= 2) {
                        put(id, arr.getDouble(0).toFloat() to arr.getDouble(1).toFloat())
                    }
                }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /** {アクセサリーID -> (fx, fy)} を位置JSON文字列に変換する。 */
    fun positionsToJson(positions: Map<Int, Pair<Float, Float>>): String {
        val obj = JSONObject()
        for ((id, p) in positions) {
            obj.put(id.toString(), JSONArray().put(p.first.toDouble()).put(p.second.toDouble()))
        }
        return obj.toString()
    }
}
