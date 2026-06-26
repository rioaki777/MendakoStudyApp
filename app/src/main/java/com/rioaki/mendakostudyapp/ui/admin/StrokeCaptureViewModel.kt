package com.rioaki.mendakostudyapp.ui.admin

import android.app.Application
import android.graphics.PointF
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rioaki.mendakostudyapp.data.stroke.StrokeRepository
import org.json.JSONObject

/**
 * 手本データ(hiragana_strokes.json)キャプチャツール用 ViewModel。
 * 既存 JSON をメモリに読み込み、なぞって取得した keyPoints で上書きし、
 * 完成した JSON 全体を文字列として書き出す。
 */
class StrokeCaptureViewModel(application: Application) : AndroidViewModel(application) {

    // 編集対象の文字一覧(五十音 + 濁点・半濁点の符号)。既存になければ新規追加扱いになる。
    // 濁音・半濁音(が/ぱ等)はベース清音 + 符号で合成するため、符号(゛/゜)だけを末尾でなぞる。
    val chars: List<Char> = (
        "あいうえお" + "かきくけこ" + "さしすせそ" + "たちつてと" + "なにぬねの" +
        "はひふへほ" + "まみむめも" + "やゆよ" + "らりるれろ" + "わをん" +
        "${StrokeRepository.DAKUTEN}${StrokeRepository.HANDAKUTEN}"
    ).toList()

    // char -> 各ストロークの keyPoints。既存 JSON から初期化。
    private val data = linkedMapOf<Char, List<List<PointF>>>()

    private var index = 0

    private val _currentChar = MutableLiveData<Char>()
    val currentChar: LiveData<Char> = _currentChar

    private val _position = MutableLiveData<Pair<Int, Int>>()
    val position: LiveData<Pair<Int, Int>> = _position

    private val _savedStrokeCount = MutableLiveData(0)
    val savedStrokeCount: LiveData<Int> = _savedStrokeCount

    init {
        loadExisting()
        updateCurrent()
    }

    private fun loadExisting() {
        try {
            val json = getApplication<Application>().assets
                .open("stroke_data/hiragana_strokes.json")
                .bufferedReader().use { it.readText() }
            val chars = JSONObject(json).getJSONArray("characters")
            for (i in 0 until chars.length()) {
                val entry = chars.getJSONObject(i)
                val ch = entry.getString("char")[0]
                val strokesJson = entry.getJSONArray("strokes")
                val strokes = (0 until strokesJson.length()).map { j ->
                    val pts = strokesJson.getJSONObject(j).getJSONArray("keyPoints")
                    (0 until pts.length()).map { k ->
                        val p = pts.getJSONObject(k)
                        PointF(p.getDouble("x").toFloat(), p.getDouble("y").toFloat())
                    }
                }
                data[ch] = strokes
            }
        } catch (_: Exception) {
            // 読み込み失敗時は空から開始
        }
    }

    private fun updateCurrent() {
        val ch = chars[index]
        _currentChar.value = ch
        _position.value = (index + 1) to chars.size
        _savedStrokeCount.value = data[ch]?.size ?: 0
    }

    fun next() {
        if (index < chars.size - 1) {
            index++
            updateCurrent()
        }
    }

    fun prev() {
        if (index > 0) {
            index--
            updateCurrent()
        }
    }

    /** 現在の文字の手本を、なぞって取得した keyPoints で上書き保存。 */
    fun saveCurrent(strokes: List<List<PointF>>) {
        if (strokes.isEmpty()) return
        data[chars[index]] = strokes
        _savedStrokeCount.value = strokes.size
    }

    /** 現在の文字の保存済みデータを削除。 */
    fun clearCurrent() {
        data.remove(chars[index])
        _savedStrokeCount.value = 0
    }

    fun hasData(char: Char): Boolean = data[char]?.isNotEmpty() == true

    /** 保存済みの全文字を hiragana_strokes.json 形式の文字列に書き出す。 */
    fun exportJson(): String {
        val sb = StringBuilder()
        sb.append("{\n  \"characters\": [\n")
        val entries = data.entries.filter { it.value.isNotEmpty() }
        entries.forEachIndexed { ci, (ch, strokes) ->
            sb.append("    {\n")
            sb.append("      \"char\": \"").append(ch).append("\",\n")
            sb.append("      \"strokes\": [\n")
            strokes.forEachIndexed { si, keyPoints ->
                val pts = keyPoints.joinToString(",") { "{\"x\":${fmt(it.x)},\"y\":${fmt(it.y)}}" }
                sb.append("        { \"order\": ").append(si + 1)
                    .append(", \"keyPoints\": [").append(pts).append("] }")
                sb.append(if (si == strokes.lastIndex) "\n" else ",\n")
            }
            sb.append("      ]\n")
            sb.append(if (ci == entries.lastIndex) "    }\n" else "    },\n")
        }
        sb.append("  ]\n}\n")
        return sb.toString()
    }

    private fun fmt(v: Float): String {
        val s = String.format("%.2f", v)
        return s
    }
}
