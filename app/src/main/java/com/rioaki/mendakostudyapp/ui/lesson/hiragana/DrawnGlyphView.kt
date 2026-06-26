package com.rioaki.mendakostudyapp.ui.lesson.hiragana

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.rioaki.mendakostudyapp.R
import kotlin.math.min

/**
 * 結果画面で「ユーザーが実際に書いた1文字」を表示する軽量ビュー。
 * 薄いお手本文字を背景に、ユーザーの線(0〜1正規化)を重ねて静的描画する。
 */
class DrawnGlyphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var guideChar: Char? = null
    private var strokes: List<List<PointF>> = emptyList()

    private val guideTypeface: Typeface? =
        ResourcesCompat.getFont(context, R.font.klee_one_semibold)

    private val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BBBBBB")
        textAlign = Paint.Align.CENTER
        alpha = 40
        typeface = guideTypeface
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A237E")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    fun setGlyph(char: Char, strokes: List<List<PointF>>) {
        this.guideChar = char
        this.strokes = strokes
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = min(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
        setMeasuredDimension(size, size)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        guidePaint.textSize = w * 0.75f
        strokePaint.strokeWidth = w * 0.04f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        guideChar?.let { canvas.drawText(it.toString(), w / 2f, h * 0.82f, guidePaint) }

        for (stroke in strokes) {
            if (stroke.size < 2) continue
            val path = Path()
            path.moveTo(stroke[0].x * w, stroke[0].y * h)
            for (i in 1 until stroke.size) {
                path.lineTo(stroke[i].x * w, stroke[i].y * h)
            }
            canvas.drawPath(path, strokePaint)
        }
    }
}
