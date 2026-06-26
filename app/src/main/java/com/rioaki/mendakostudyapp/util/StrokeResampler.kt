package com.rioaki.mendakostudyapp.util

import android.graphics.PointF
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * なぞり軌跡を一定数の keyPoints に等間隔(弧長基準)で間引くユーティリティ。
 * キャプチャツールで手本データ(hiragana_strokes.json)を生成するために使う。
 */
object StrokeResampler {

    /**
     * 0〜1 正規化済みの軌跡を、弧長 spacing ごとに 1 点となるよう等間隔リサンプルする。
     * 点数を長さに応じて自動決定するため、長いループは多く・短い画は少なく点が並び、
     * 曲線(特にループ)が字形に沿って忠実に表現される。count 固定方式より曲線再現が高精度。
     */
    fun resampleBySpacing(
        points: List<PointF>,
        spacing: Float = 0.05f,
        minCount: Int = 3,
        maxCount: Int = 48
    ): List<PointF> {
        if (points.isEmpty()) return emptyList()
        var total = 0.0
        for (i in 1 until points.size) {
            total += hypot(
                (points[i].x - points[i - 1].x).toDouble(),
                (points[i].y - points[i - 1].y).toDouble()
            )
        }
        val count = ((total / spacing).roundToInt() + 1).coerceIn(minCount, maxCount)
        return resample(points, count)
    }

    /** 0〜1 正規化済みの軌跡を count 個の keyPoints に等間隔リサンプルする。 */
    fun resample(points: List<PointF>, count: Int): List<PointF> {
        if (points.isEmpty()) return emptyList()
        if (points.size <= count) return points.map { round2(it) }

        val cum = DoubleArray(points.size)
        for (i in 1 until points.size) {
            val dx = (points[i].x - points[i - 1].x).toDouble()
            val dy = (points[i].y - points[i - 1].y).toDouble()
            cum[i] = cum[i - 1] + hypot(dx, dy)
        }
        val total = cum.last()
        if (total == 0.0) return List(count) { round2(points.first()) }

        val result = ArrayList<PointF>(count)
        for (k in 0 until count) {
            val target = total * k / (count - 1)
            var i = 1
            while (i < points.size && cum[i] < target) i++
            if (i >= points.size) i = points.size - 1
            val segLen = cum[i] - cum[i - 1]
            val t = if (segLen == 0.0) 0.0 else (target - cum[i - 1]) / segLen
            val x = points[i - 1].x + (points[i].x - points[i - 1].x) * t.toFloat()
            val y = points[i - 1].y + (points[i].y - points[i - 1].y) * t.toFloat()
            result.add(PointF(round2f(x), round2f(y)))
        }
        return result
    }

    private fun round2(p: PointF): PointF = PointF(round2f(p.x), round2f(p.y))
    private fun round2f(v: Float): Float = (v * 100f).roundToInt() / 100f
}
