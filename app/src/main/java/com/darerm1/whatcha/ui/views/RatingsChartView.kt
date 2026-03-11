package com.darerm1.whatcha.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.darerm1.whatcha.data.models.MovieRatings
import com.google.android.material.color.MaterialColors
import kotlin.math.max
import kotlin.math.min

class RatingsChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var ratings: MovieRatings? = null
        set(value) {
            field = value
            invalidate()
        }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x4DBFA2E9
        strokeWidth = 1.dp.toFloat()
    }

    private val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnSurface, 0xFFF5F2FF.toInt())
        textAlign = Paint.Align.CENTER
        textSize = 12.sp.toFloat()
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xCCCEC1F1.toInt()
        textAlign = Paint.Align.CENTER
        textSize = 11.sp.toFloat()
    }

    private val emptyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xCCCEC1F1.toInt()
        textAlign = Paint.Align.CENTER
        textSize = 14.sp.toFloat()
    }

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val barColors = intArrayOf(
        0xFFDCC8FF.toInt(),
        0xFFC9AEF4.toInt(),
        0xFFBFA2E9.toInt(),
        0xFFA786DD.toInt(),
        0xFF8D6FD0.toInt(),
        0xFF7658BE.toInt()
    )

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = 220.dp + paddingTop + paddingBottom
        setMeasuredDimension(
            resolveSize(320.dp + paddingLeft + paddingRight, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val entries = listOfNotNull(
            ratings?.kp?.takeIf { it > 0 }?.let { ChartEntry("KP", it.toFloat()) },
            ratings?.imdb?.takeIf { it > 0 }?.let { ChartEntry("IMDb", it.toFloat()) },
            ratings?.tmdb?.takeIf { it > 0 }?.let { ChartEntry("TMDb", it.toFloat()) },
            ratings?.filmCritics?.takeIf { it > 0 }?.let { ChartEntry("Crit", it.toFloat()) },
            ratings?.russianFilmCritics?.takeIf { it > 0 }?.let { ChartEntry("RuC", it.toFloat()) },
            ratings?.await?.takeIf { it > 0 }?.let { ChartEntry("Await", it.toFloat()) }
        )

        if (entries.isEmpty()) {
            val x = width / 2f
            val y = height / 2f - (emptyPaint.descent() + emptyPaint.ascent()) / 2f
            canvas.drawText(NO_DATA_TEXT, x, y, emptyPaint)
            return
        }

        val left = paddingLeft + 8.dp.toFloat()
        val right = width - paddingRight - 8.dp.toFloat()
        val top = paddingTop + 20.dp.toFloat()
        val bottom = height - paddingBottom - 28.dp.toFloat()
        val chartHeight = max(1f, bottom - top)
        val chartWidth = max(1f, right - left)
        val slotWidth = chartWidth / entries.size
        val barWidth = min(32.dp.toFloat(), slotWidth * 0.58f)

        canvas.drawLine(left, bottom, right, bottom, axisPaint)

        entries.forEachIndexed { index, entry ->
            val centerX = left + slotWidth * index + slotWidth / 2f
            val barHeight = (entry.value.coerceIn(0f, 10f) / 10f) * chartHeight
            val barTop = bottom - barHeight
            val barLeft = centerX - barWidth / 2f
            val barRight = centerX + barWidth / 2f

            barPaint.color = barColors[index % barColors.size]
            canvas.drawRoundRect(barLeft, barTop, barRight, bottom, 12.dp.toFloat(), 12.dp.toFloat(), barPaint)

            val valueText = String.format(java.util.Locale.US, "%.1f", entry.value)
            canvas.drawText(valueText, centerX, barTop - 8.dp, valuePaint)
            canvas.drawText(entry.label, centerX, bottom + 18.dp, labelPaint)
        }
    }

    private data class ChartEntry(
        val label: String,
        val value: Float
    )

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private val Int.sp: Int
        get() = (this * resources.displayMetrics.scaledDensity).toInt()

    companion object {
        private const val NO_DATA_TEXT = "\u041d\u0435\u0442 \u0434\u0430\u043d\u043d\u044b\u0445 \u0434\u043b\u044f \u0433\u0440\u0430\u0444\u0438\u043a\u0430"
    }
}