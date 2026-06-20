package com.rioaki.mendakostudyapp.ui.lesson.hiragana

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.rioaki.mendakostudyapp.data.stroke.HiraganaStrokeData
import kotlin.math.min

class HiraganaCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var onStrokeCompleted: ((List<PointF>) -> Unit)? = null

    private val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BBBBBB")
        textAlign = Paint.Align.CENTER
        alpha = 40
    }

    private val orderNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        color = Color.WHITE
    }

    private val circleDonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#BDBDBD")
    }

    private val circleNextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#F44336")
    }

    private val circleFuturePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFCDD2")
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A237E")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val pendingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3949AB")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private var guideChar: Char? = null
    private var strokeData: HiraganaStrokeData? = null
    private var expectedStrokeIndex: Int = 0

    private var completedStrokes: List<List<PointF>> = emptyList()
    private var pendingStroke: List<PointF>? = null
    private val currentPoints = mutableListOf<PointF>()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = min(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
        setMeasuredDimension(size, size)
    }

    fun setGuideChar(char: Char, data: HiraganaStrokeData?, nextStrokeIndex: Int) {
        guideChar = char
        strokeData = data
        expectedStrokeIndex = nextStrokeIndex
        invalidate()
    }

    fun setCompletedStrokes(strokes: List<List<PointF>>, nextStrokeIndex: Int) {
        completedStrokes = strokes
        expectedStrokeIndex = nextStrokeIndex
        pendingStroke = null
        invalidate()
    }

    fun clearPendingStroke() {
        pendingStroke = null
        invalidate()
    }

    fun resetCanvas() {
        completedStrokes = emptyList()
        pendingStroke = null
        currentPoints.clear()
        expectedStrokeIndex = 0
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val strokeWidth = w * 0.04f
        strokePaint.strokeWidth = strokeWidth
        pendingPaint.strokeWidth = strokeWidth
        guidePaint.textSize = w * 0.75f
        orderNumberPaint.textSize = w * 0.10f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Background guide character
        guideChar?.let { ch ->
            canvas.drawText(ch.toString(), w / 2f, h * 0.82f, guidePaint)
        }

        // Stroke order numbers
        strokeData?.let { data ->
            val circleRadius = w * 0.065f
            data.strokes.forEachIndexed { index, stroke ->
                val startPt = stroke.keyPoints.first()
                val px = startPt.x * w
                val py = startPt.y * h
                val paint = when {
                    index < expectedStrokeIndex -> circleDonePaint
                    index == expectedStrokeIndex -> circleNextPaint
                    else -> circleFuturePaint
                }
                canvas.drawCircle(px, py, circleRadius, paint)
                canvas.drawText(
                    stroke.order.toString(),
                    px,
                    py + orderNumberPaint.textSize * 0.35f,
                    orderNumberPaint
                )
            }
        }

        // Completed strokes
        for (stroke in completedStrokes) {
            if (stroke.size < 2) continue
            val path = buildPath(stroke)
            canvas.drawPath(path, strokePaint)
        }

        // Pending stroke (after finger up, awaiting judgment)
        pendingStroke?.let { stroke ->
            if (stroke.size >= 2) {
                canvas.drawPath(buildPath(stroke), pendingPaint)
            }
        }

        // Current in-progress stroke
        if (currentPoints.size >= 2) {
            canvas.drawPath(buildPath(currentPoints), pendingPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentPoints.clear()
                currentPoints.add(PointF(event.x, event.y))
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                currentPoints.add(PointF(event.x, event.y))
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                currentPoints.add(PointF(event.x, event.y))
                val stroke = currentPoints.toList()
                currentPoints.clear()
                pendingStroke = stroke
                invalidate()
                onStrokeCompleted?.invoke(stroke)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun buildPath(points: List<PointF>): Path {
        val path = Path()
        path.moveTo(points[0].x, points[0].y)
        for (i in 1 until points.size) {
            path.lineTo(points[i].x, points[i].y)
        }
        return path
    }
}
