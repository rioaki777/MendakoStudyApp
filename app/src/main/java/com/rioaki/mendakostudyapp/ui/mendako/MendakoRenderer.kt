package com.rioaki.mendakostudyapp.ui.mendako

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.data.model.MendakoCatalog
import com.rioaki.mendakostudyapp.data.model.MendakoDef
import com.rioaki.mendakostudyapp.ui.mendako.AccessoryCatalog.AccessoryDef
import org.json.JSONArray
import org.json.JSONObject

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

    /** 位置・サイズ計算の基準とするコンテナ一辺(dp)。アクセサリー編集画面のコンテナと一致させる。 */
    const val REFERENCE_SIZE_DP = 300

    /** 動的生成したアクセサリーViewを識別するためのタグ接頭辞（"accessory_<id>"）。 */
    const val ACCESSORY_TAG_PREFIX = "accessory_"

    /** ViewのタグからアクセサリーIDを取り出す（アクセサリーView以外は null）。 */
    fun accessoryIdOf(view: View): Int? =
        (view.tag as? String)?.takeIf { it.startsWith(ACCESSORY_TAG_PREFIX) }
            ?.removePrefix(ACCESSORY_TAG_PREFIX)?.toIntOrNull()

    /**
     * メンダコ本体コンテナ([container] = body/eyes/mouth を含む FrameLayout)に、
     * 装備中アクセサリーぶんの ImageView を動的に生成/更新/削除して重ね描画する。
     * 各アクセサリーの既定サイズ・位置は [AccessoryCatalog] を参照し、
     * [positions]（コンテナサイズに対するオフセット比率）があれば基準位置からの translation を適用する。
     */
    fun applyAccessories(
        container: ViewGroup,
        equippedIds: List<Int>,
        positions: Map<Int, Pair<Float, Float>> = emptyMap()
    ) {
        // 装備解除された（または定義のない）アクセサリーViewを取り除く。
        val stale = (0 until container.childCount)
            .map { container.getChildAt(it) }
            .filter { v -> accessoryIdOf(v)?.let { it !in equippedIds } ?: false }
        stale.forEach { container.removeView(it) }

        // 装備中アクセサリーを生成（既存があれば再利用）し、サイズ・位置を適用する。
        for (id in equippedIds) {
            val def = AccessoryCatalog.byId(id) ?: continue
            val view = findAccessoryView(container, id) ?: createAccessoryView(container, def)
            if (container.width > 0 && container.height > 0) {
                applyAccessoryLayout(view, container, def, positions[id])
            } else {
                // コンテナが 0dp(計測で確定)の画面では計測後に適用する。
                container.post { applyAccessoryLayout(view, container, def, positions[id]) }
            }
        }
    }

    private fun findAccessoryView(container: ViewGroup, id: Int): ImageView? {
        for (i in 0 until container.childCount) {
            val v = container.getChildAt(i)
            if (v is ImageView && accessoryIdOf(v) == id) return v
        }
        return null
    }

    private fun createAccessoryView(container: ViewGroup, def: AccessoryDef): ImageView {
        val view = ImageView(container.context)
        view.tag = ACCESSORY_TAG_PREFIX + def.id
        view.contentDescription = null
        val ctx = container.context
        val resId = ctx.resources.getIdentifier(def.imageResName, "drawable", ctx.packageName)
        if (resId != 0) view.setImageResource(resId)
        container.addView(view, FrameLayout.LayoutParams(0, 0))
        return view
    }

    /** 計測済みのコンテナサイズを基準に、アクセサリーのサイズ(比例)・基準位置・移動量(比率)を適用する。 */
    private fun applyAccessoryLayout(
        view: ImageView,
        container: ViewGroup,
        def: AccessoryDef,
        pos: Pair<Float, Float>?
    ) {
        val basisW = container.width
        val basisH = container.height
        if (basisW <= 0 || basisH <= 0) return

        // コンテナが基準サイズより大きい/小さい場合でも見た目が一致するよう、固定dpサイズを比例させる。
        val density = view.resources.displayMetrics.density
        val scale = basisW / (REFERENCE_SIZE_DP * density)
        val lp = (view.layoutParams as? FrameLayout.LayoutParams)
            ?: FrameLayout.LayoutParams(0, 0)
        lp.width = (def.widthDp * density * scale).toInt()
        lp.height = (def.heightDp * density * scale).toInt()
        lp.gravity = def.gravity
        lp.topMargin = (def.marginTopDp * density * scale).toInt()
        lp.marginEnd = (def.marginEndDp * density * scale).toInt()
        view.layoutParams = lp

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
