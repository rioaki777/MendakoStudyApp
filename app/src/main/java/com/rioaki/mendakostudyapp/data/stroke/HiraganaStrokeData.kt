package com.rioaki.mendakostudyapp.data.stroke

import android.graphics.PointF

data class HiraganaStrokeData(
    val char: Char,
    val strokes: List<StrokePath>
)

data class StrokePath(
    val order: Int,
    val keyPoints: List<PointF>
)
