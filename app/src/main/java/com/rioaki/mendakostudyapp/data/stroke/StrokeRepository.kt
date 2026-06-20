package com.rioaki.mendakostudyapp.data.stroke

import android.content.res.AssetManager
import android.graphics.PointF
import org.json.JSONObject

object StrokeRepository {

    private val cache = mutableMapOf<Char, HiraganaStrokeData>()

    fun load(assets: AssetManager) {
        if (cache.isNotEmpty()) return
        val json = assets.open("stroke_data/hiragana_strokes.json")
            .bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val chars = root.getJSONArray("characters")
        for (i in 0 until chars.length()) {
            val entry = chars.getJSONObject(i)
            val ch = entry.getString("char")[0]
            val strokesJson = entry.getJSONArray("strokes")
            val strokes = (0 until strokesJson.length()).map { j ->
                val s = strokesJson.getJSONObject(j)
                val order = s.getInt("order")
                val pts = s.getJSONArray("keyPoints")
                val keyPoints = (0 until pts.length()).map { k ->
                    val p = pts.getJSONObject(k)
                    PointF(p.getDouble("x").toFloat(), p.getDouble("y").toFloat())
                }
                StrokePath(order, keyPoints)
            }
            cache[ch] = HiraganaStrokeData(ch, strokes)
        }
    }

    fun get(char: Char): HiraganaStrokeData? = cache[char]
}
