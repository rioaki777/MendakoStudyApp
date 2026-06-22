package com.rioaki.mendakostudyapp.ui.lesson.hiragana

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.rioaki.mendakostudyapp.R
import com.rioaki.mendakostudyapp.data.stroke.HiraganaStrokeData
import com.rioaki.mendakostudyapp.util.StrokeResampler
import kotlin.math.min

class HiraganaCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var onStrokeCompleted: ((List<PointF>) -> Unit)? = null

    // --- Capture mode (手本データ作成用) ---
    var isCaptureMode: Boolean = false
        set(value) {
            field = value
            invalidate()
        }
    var pointsPerStroke: Int = 5
    var onCaptureChanged: (() -> Unit)? = null
    private val capturedStrokes = mutableListOf<List<PointF>>()

    private val capturePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2E7D32")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val captureDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#1B5E20")
    }

    private val captureLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }

    // 幼児向けの教科書体手本フォント（とめ・はね・はらいが正しく見える）
    private val guideTypeface: Typeface? =
        ResourcesCompat.getFont(context, R.font.klee_one_semibold)

    private val guidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BBBBBB")
        textAlign = Paint.Align.CENTER
        alpha = 40
        typeface = guideTypeface
    }

    private val orderNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
        color = Color.WHITE
        typeface = guideTypeface
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

    private val debugDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FF6B35")
    }

    private val debugLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#FF6B35")
        strokeCap = Paint.Cap.ROUND
        pathEffect = DashPathEffect(floatArrayOf(12f, 8f), 0f)
    }

    private val debugLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#BF360C")
        isFakeBoldText = true
        textAlign = Paint.Align.LEFT
    }

    var isDebugMode: Boolean = false
        set(value) {
            field = value
            invalidate()
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
        debugLinePaint.strokeWidth = w * 0.015f
        debugLabelPaint.textSize = w * 0.07f
        capturePaint.strokeWidth = w * 0.03f
        captureLabelPaint.textSize = w * 0.06f
    }

    fun resetCaptures() {
        capturedStrokes.clear()
        currentPoints.clear()
        invalidate()
        onCaptureChanged?.invoke()
    }

    fun undoLastCapture() {
        if (capturedStrokes.isNotEmpty()) {
            capturedStrokes.removeAt(capturedStrokes.lastIndex)
            invalidate()
            onCaptureChanged?.invoke()
        }
    }

    fun capturedStrokeCount(): Int = capturedStrokes.size

    /** キャプチャした各ストロークを 0〜1 正規化＋等間隔リサンプルした keyPoints で返す。 */
    fun getCapturedKeyPoints(): List<List<PointF>> {
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return emptyList()
        return capturedStrokes.map { stroke ->
            val normalized = stroke.map { PointF(it.x / w, it.y / h) }
            StrokeResampler.resample(normalized, pointsPerStroke)
        }
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
            val paint = if (isCaptureMode) capturePaint else pendingPaint
            canvas.drawPath(buildPath(currentPoints), paint)
        }

        // Captured strokes preview (capture mode only)
        if (isCaptureMode) {
            val keyPointSets = getCapturedKeyPoints()
            keyPointSets.forEachIndexed { strokeIdx, keyPoints ->
                val pts = keyPoints.map { PointF(it.x * w, it.y * h) }
                if (pts.size >= 2) {
                    val path = Path()
                    path.moveTo(pts[0].x, pts[0].y)
                    for (i in 1 until pts.size) path.lineTo(pts[i].x, pts[i].y)
                    canvas.drawPath(path, capturePaint)
                }
                pts.forEachIndexed { ptIdx, pt ->
                    val radius = if (ptIdx == 0) w * 0.05f else w * 0.035f
                    canvas.drawCircle(pt.x, pt.y, radius, captureDotPaint)
                    if (ptIdx == 0) {
                        canvas.drawText(
                            (strokeIdx + 1).toString(),
                            pt.x,
                            pt.y + captureLabelPaint.textSize * 0.35f,
                            captureLabelPaint
                        )
                    }
                }
            }
        }

        // Debug overlay: all key points with order labels
        if (isDebugMode) {
            strokeData?.strokes?.forEach { stroke ->
                val pts = stroke.keyPoints.map { PointF(it.x * w, it.y * h) }
                if (pts.size >= 2) {
                    val path = Path()
                    path.moveTo(pts[0].x, pts[0].y)
                    for (i in 1 until pts.size) {
                        path.lineTo(pts[i].x, pts[i].y)
                    }
                    canvas.drawPath(path, debugLinePaint)
                }
                pts.forEachIndexed { idx, pt ->
                    canvas.drawCircle(pt.x, pt.y, w * 0.03f, debugDotPaint)
                    canvas.drawText(
                        "${stroke.order}-${idx + 1}",
                        pt.x + w * 0.035f,
                        pt.y + debugLabelPaint.textSize * 0.35f,
                        debugLabelPaint
                    )
                }
            }
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
                if (isCaptureMode) {
                    if (stroke.size >= 2) {
                        capturedStrokes.add(stroke)
                        onCaptureChanged?.invoke()
                    }
                    invalidate()
                    return true
                }
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
