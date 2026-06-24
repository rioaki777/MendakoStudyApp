package com.rioaki.mendakostudyapp.ui.mendako

import android.view.View
import android.widget.ImageView
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.data.model.MendakoCatalog
import com.rioaki.mendakostudyapp.data.model.MendakoDef
import org.json.JSONArray
import org.json.JSONObject
import java.util.WeakHashMap

/**
 * ホーム/部屋/アクセサリー画面で共通する「選択中メンダコ個体の本体＋装備」描画を集約する。
 * 本体画像は [MendakoDef.imageResName] を解決して使い、未解決（友達の専用画像が未追加）の場合は
 * デフォルト本体に [MendakoDef.placeholderColor] の色フィルタをかけて区別する。
 */
object MendakoRenderer {

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

    /** 位置・サイズ計算の基準とするコンテナ一辺(dp)。アクセサリー編集画面のコンテナと一致させる。 */
    private const val REFERENCE_SIZE_DP = 300

    /** 各 ImageView の元(=XML)サイズを保持し、比例スケール時に値が複利で膨らむのを防ぐ。 */
    private val baseSizes = WeakHashMap<View, Pair<Int, Int>>()

    private fun applyAccessory(
        view: ImageView,
        id: Int,
        equippedIds: List<Int>,
        positions: Map<Int, Pair<Float, Float>>
    ) {
        view.visibility = if (id in equippedIds) ImageView.VISIBLE else ImageView.GONE
        val parent = view.parent as? View ?: return
        // 比率座標は親のサイズに対する割合。コンテナが 0dp(計測で確定)の画面では
        // レイアウト前に呼ばれると幅が 0 になり移動量が無視されるため、未計測なら計測後に適用する。
        if (parent.width > 0 && parent.height > 0) {
            applyAccessoryLayout(view, parent, positions[id])
        } else {
            parent.post { applyAccessoryLayout(view, parent, positions[id]) }
        }
    }

    /** 計測済みの親サイズを基準に、アクセサリーのサイズ(比例)と移動量(比率)を適用する。 */
    private fun applyAccessoryLayout(view: ImageView, parent: View, pos: Pair<Float, Float>?) {
        val basisW = parent.width
        val basisH = parent.height
        if (basisW <= 0 || basisH <= 0) return

        // コンテナが基準サイズより大きい/小さい場合でも見た目が一致するよう、固定dpサイズを比例させる。
        val refPx = REFERENCE_SIZE_DP * view.resources.displayMetrics.density
        val scale = basisW / refPx
        val (baseW, baseH) = baseSizes.getOrPut(view) {
            view.layoutParams.width to view.layoutParams.height
        }
        if (baseW > 0 && baseH > 0) {
            val lp = view.layoutParams
            lp.width = (baseW * scale).toInt()
            lp.height = (baseH * scale).toInt()
            view.layoutParams = lp
        }

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
