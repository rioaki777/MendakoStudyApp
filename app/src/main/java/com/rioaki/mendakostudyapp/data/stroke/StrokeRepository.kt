package com.rioaki.mendakostudyapp.data.stroke

import android.content.res.AssetManager
import android.graphics.PointF
import org.json.JSONObject
import java.text.Normalizer

object StrokeRepository {

    private val cache = mutableMapOf<Char, HiraganaStrokeData>()

    // 符号の手本データを JSON に格納する際のキー（表示用の独立した濁点・半濁点）
    const val DAKUTEN = '゛'      // ゛
    const val HANDAKUTEN = '゜'   // ゜

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

    fun get(char: Char): HiraganaStrokeData? {
        cache[char]?.let { return it }

        // 濁音・半濁音は清音(ベース)の手本に符号(゛/゜)を最後の画として連結して合成する。
        // NFD 正規化で「が」→「か」+ U+3099(結合濁点) のように分解できるため対応表は不要。
        val nfd = Normalizer.normalize(char.toString(), Normalizer.Form.NFD)
        if (nfd.length == 2) {
            val base = cache[nfd[0]] ?: return null
            val markChar = when (nfd[1].code) {
                0x3099 -> DAKUTEN      // 結合濁点 → 独立濁点キー
                0x309A -> HANDAKUTEN   // 結合半濁点 → 独立半濁点キー
                else -> return null
            }
            val mark = cache[markChar] ?: return null
            // 符号の画は order をベースの続き番号に振り直して連結
            val combined = base.strokes + mark.strokes.mapIndexed { i, s ->
                s.copy(order = base.strokes.size + i + 1)
            }
            return HiraganaStrokeData(char, combined).also { cache[char] = it }
        }
        return null
    }
}
