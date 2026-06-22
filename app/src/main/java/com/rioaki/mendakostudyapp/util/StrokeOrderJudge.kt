package com.rioaki.mendakostudyapp.util

import android.graphics.PointF
import com.rioaki.mendakostudyapp.data.stroke.HiraganaStrokeData
import com.rioaki.mendakostudyapp.data.stroke.StrokePath
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

object StrokeOrderJudge {

    // 始点が手本の始点にどれだけ近ければよいか(書き始めを甘めに)
    private const val START_THRESHOLD = 0.20f
    // 終点が手本の終点にどれだけ近ければよいか(はらい・はね終わりを甘めに)
    private const val END_THRESHOLD = 0.22f
    // ユーザの軌跡が手本の経路からどれだけ外れてよいか(迂回・はみ出し検出)
    private const val ON_PATH_THRESHOLD = 0.11f
    // 手本の経路をどれだけなぞり切る必要があるか(ショートカット・直線抜け検出)
    private const val COVERAGE_THRESHOLD = 0.13f
    // 外れ値(指のブレ)を許容するためのパーセンタイル。1.0=最大値で最も厳しい
    private const val PERCENTILE = 0.95f
    // 手本経路を点列に細分化する際のステップ(正規化座標)
    private const val SAMPLE_STEP = 0.02f

    fun judgeStroke(
        userPath: List<PointF>,
        expected: StrokePath,
        canvasWidth: Int,
        canvasHeight: Int
    ): Boolean {
        if (userPath.size < 2 || canvasWidth <= 0 || canvasHeight <= 0) return false
        val user = normalizePoints(userPath, canvasWidth, canvasHeight)
        val key = expected.keyPoints
        if (key.isEmpty()) return true
        if (key.size == 1) {
            return distance(user.first(), key.first()) < START_THRESHOLD
        }

        // 手本の keyPoints を結んだ折れ線を細かい点列に分解
        val expectedSamples = densify(key, SAMPLE_STEP)

        val startOk = distance(user.first(), key.first()) < START_THRESHOLD
        val endOk = distance(user.last(), key.last()) < END_THRESHOLD
        // 各ユーザ点が手本経路の近くにあるか(迂回・はみ出していないか)
        val onPathOk = directedDistance(user, expectedSamples, PERCENTILE) < ON_PATH_THRESHOLD
        // 手本経路の各点をユーザがなぞったか(ショートカットしていないか)
        val coverageOk = directedDistance(expectedSamples, user, PERCENTILE) < COVERAGE_THRESHOLD

        return startOk && endOk && onPathOk && coverageOk
    }

    fun judge(
        userStrokes: List<List<PointF>>,
        expected: HiraganaStrokeData,
        canvasWidth: Int,
        canvasHeight: Int
    ): Boolean {
        if (userStrokes.size != expected.strokes.size) return false
        return userStrokes.zip(expected.strokes).all { (userStroke, expectedStroke) ->
            judgeStroke(userStroke, expectedStroke, canvasWidth, canvasHeight)
        }
    }

    private fun normalizePoints(points: List<PointF>, w: Int, h: Int): List<PointF> =
        points.map { PointF(it.x / w, it.y / h) }

    private fun distance(a: PointF, b: PointF): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return sqrt(dx * dx + dy * dy)
    }

    /** 折れ線を SAMPLE_STEP 間隔の点列に細分化する。 */
    private fun densify(points: List<PointF>, step: Float): List<PointF> {
        val out = ArrayList<PointF>()
        for (i in 0 until points.size - 1) {
            val a = points[i]
            val b = points[i + 1]
            val segLen = distance(a, b)
            val n = max(1, ceil(segLen / step).toInt())
            for (j in 0 until n) {
                val t = j.toFloat() / n
                out.add(PointF(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t))
            }
        }
        out.add(points.last())
        return out
    }

    /**
     * from の各点から to の最寄り点までの距離を求め、その PERCENTILE 値を返す
     * (有向ハウスドルフ距離のパーセンタイル版)。外れ値を許容しつつ系統的なズレを検出する。
     */
    private fun directedDistance(from: List<PointF>, to: List<PointF>, percentile: Float): Float {
        if (from.isEmpty() || to.isEmpty()) return Float.MAX_VALUE
        val dists = from.map { f -> to.minOf { t -> distance(f, t) } }.sorted()
        val idx = ((dists.size - 1) * percentile).roundToInt().coerceIn(0, dists.size - 1)
        return dists[idx]
    }
}
