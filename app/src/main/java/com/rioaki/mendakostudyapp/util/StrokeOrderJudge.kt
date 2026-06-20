package com.rioaki.mendakostudyapp.util

import android.graphics.PointF
import com.rioaki.mendakostudyapp.data.stroke.HiraganaStrokeData
import com.rioaki.mendakostudyapp.data.stroke.StrokePath
import kotlin.math.sqrt

object StrokeOrderJudge {

    private const val ENDPOINT_THRESHOLD = 0.15f
    private const val MIDPOINT_THRESHOLD = 0.20f

    fun judgeStroke(
        userPath: List<PointF>,
        expected: StrokePath,
        canvasWidth: Int,
        canvasHeight: Int
    ): Boolean {
        if (userPath.size < 2 || canvasWidth <= 0 || canvasHeight <= 0) return false
        val normalized = normalizePoints(userPath, canvasWidth, canvasHeight)
        val startOk = distance(normalized.first(), expected.keyPoints.first()) < ENDPOINT_THRESHOLD
        val endOk = distance(normalized.last(), expected.keyPoints.last()) < ENDPOINT_THRESHOLD
        val midOk = averageMinDistance(normalized, expected.keyPoints) < MIDPOINT_THRESHOLD
        return startOk && endOk && midOk
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

    private fun averageMinDistance(userPoints: List<PointF>, keyPoints: List<PointF>): Float {
        if (keyPoints.isEmpty()) return Float.MAX_VALUE
        val sum = userPoints.sumOf { u ->
            keyPoints.minOf { k -> distance(u, k) }.toDouble()
        }
        return (sum / userPoints.size).toFloat()
    }
}
