package com.darerm1.whatcha.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class SkySceneView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    enum class Phase {
        MORNING,
        DAY,
        EVENING,
        NIGHT
    }

    var phase: Phase = Phase.DAY
        set(value) {
            field = value
            invalidate()
        }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val orbPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cloudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0x42F2E8FF
    }
    private val hillBackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val hillFrontPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xE6F6F0FF.toInt()
    }

    private val nightStars = listOf(
        StarSpec(0.10f, 0.16f, 1.7f, 0.02f),
        StarSpec(0.22f, 0.22f, 2.1f, 0.17f),
        StarSpec(0.37f, 0.14f, 1.8f, 0.31f),
        StarSpec(0.58f, 0.20f, 1.6f, 0.46f),
        StarSpec(0.74f, 0.12f, 2.2f, 0.59f),
        StarSpec(0.86f, 0.26f, 1.9f, 0.71f),
        StarSpec(0.16f, 0.38f, 1.8f, 0.27f),
        StarSpec(0.45f, 0.34f, 2.0f, 0.63f),
        StarSpec(0.69f, 0.40f, 1.7f, 0.84f)
    )

    private var animationProgress = 0f
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 9000L
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            animationProgress = it.animatedValue as Float
            invalidate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!animator.isStarted) animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        drawBackground(canvas, w, h)
        drawAtmosphere(canvas, w, h)

        when (phase) {
            Phase.MORNING -> drawSunScene(canvas, w, h, 0.24f, 0.27f, 0xFFFFE0B0.toInt(), 0xFFD39AF5.toInt())
            Phase.DAY -> drawSunScene(canvas, w, h, 0.5f, 0.18f, 0xFFFFF3D4.toInt(), 0xFFCFB3FF.toInt())
            Phase.EVENING -> drawSunsetScene(canvas, w, h)
            Phase.NIGHT -> drawNightScene(canvas, w, h)
        }

        drawForeground(canvas, w, h)
    }

    private fun drawBackground(canvas: Canvas, w: Float, h: Float) {
        val colors = when (phase) {
            Phase.MORNING -> intArrayOf(0xFF4C4D86.toInt(), 0xFFC78CB0.toInt(), 0xFFF3D6B6.toInt())
            Phase.DAY -> intArrayOf(0xFF4A4E8F.toInt(), 0xFF8F84D8.toInt(), 0xFFE9DCF8.toInt())
            Phase.EVENING -> intArrayOf(0xFF241D49.toInt(), 0xFF7D4C7F.toInt(), 0xFFE19A78.toInt())
            Phase.NIGHT -> intArrayOf(0xFF0E0C1D.toInt(), 0xFF171633.toInt(), 0xFF222047.toInt())
        }

        backgroundPaint.shader = LinearGradient(0f, 0f, 0f, h, colors, null, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, w, h, backgroundPaint)
    }

    private fun drawAtmosphere(canvas: Canvas, w: Float, h: Float) {
        val pulse = 0.5f + 0.5f * sin(animationProgress * PI * 2f).toFloat()
        val glowY = when (phase) {
            Phase.MORNING -> h * 0.8f
            Phase.DAY -> h * 0.2f
            Phase.EVENING -> h * 0.86f
            Phase.NIGHT -> h * 0.72f
        }

        val glowColor = when (phase) {
            Phase.MORNING -> 0x28F6D3F1
            Phase.DAY -> 0x20DDD1FF
            Phase.EVENING -> 0x2EF0A37A
            Phase.NIGHT -> 0x143E3266
        }.toInt()

        glowPaint.shader = RadialGradient(
            w * 0.5f,
            glowY,
            w * (0.58f + pulse * 0.04f),
            intArrayOf(glowColor, 0x00000000),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRect(0f, 0f, w, h, glowPaint)
    }

    private fun drawSunScene(
        canvas: Canvas,
        w: Float,
        h: Float,
        xRatio: Float,
        yRatio: Float,
        innerColor: Int,
        outerColor: Int
    ) {
        val drift = sin(animationProgress * PI * 2f).toFloat()
        val centerX = w * xRatio + drift * 10.dp
        val centerY = h * yRatio + cos(animationProgress * PI * 2f).toFloat() * 6.dp
        val radius = 34.dp

        orbPaint.shader = RadialGradient(
            centerX,
            centerY,
            radius * 2.3f,
            intArrayOf(innerColor, outerColor),
            floatArrayOf(0.24f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(centerX, centerY, radius, orbPaint)

        drawCloudBand(canvas, w, h, drift)
    }

    private fun drawSunsetScene(canvas: Canvas, w: Float, h: Float) {
        val drift = sin(animationProgress * PI * 2f).toFloat()
        val centerX = w * 0.74f + drift * 8.dp
        val centerY = h * 0.67f
        val radius = 38.dp

        orbPaint.shader = RadialGradient(
            centerX,
            centerY,
            radius * 2.5f,
            intArrayOf(0xFFFFD8A2.toInt(), 0xFFE08AA0.toInt()),
            floatArrayOf(0.18f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(centerX, centerY, radius, orbPaint)

        drawCloud(canvas, w * 0.16f + drift * 8.dp, h * 0.28f, 26.dp)
        drawCloud(canvas, w * 0.5f - drift * 10.dp, h * 0.42f, 32.dp)
        drawCloud(canvas, w * 0.72f + drift * 5.dp, h * 0.58f, 24.dp)
    }

    private fun drawNightScene(canvas: Canvas, w: Float, h: Float) {
        val wave = sin(animationProgress * PI * 2f).toFloat()
        val moonX = w * 0.78f + wave * 6.dp
        val moonY = h * 0.22f + wave * 3.dp
        val radius = 29.dp

        orbPaint.shader = null
        orbPaint.color = 0xFFF4F0FF.toInt()
        canvas.drawCircle(moonX, moonY, radius, orbPaint)

        nightStars.forEach { star ->
            val twinkle = sin((animationProgress + star.offset) * PI * 2f).toFloat()
            starPaint.alpha = (110 + twinkle * 100f).toInt().coerceIn(70, 220)
            canvas.drawCircle(
                w * star.x,
                h * star.y + twinkle * 1.5f.dp,
                star.size.dp,
                starPaint
            )
        }
    }

    private fun drawForeground(canvas: Canvas, w: Float, h: Float) {
        val backColor = when (phase) {
            Phase.MORNING -> 0x6631254C
            Phase.DAY -> 0x55302A53
            Phase.EVENING -> 0x7A211B38
            Phase.NIGHT -> 0xAA0C0B1E
        }.toInt()
        val frontColor = when (phase) {
            Phase.MORNING -> 0x8A261D40
            Phase.DAY -> 0x7A272246
            Phase.EVENING -> 0xA01A142D
            Phase.NIGHT -> 0xD0070613
        }.toInt()

        hillBackPaint.color = backColor
        hillFrontPaint.color = frontColor

        val backPath = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.82f)
            cubicTo(w * 0.12f, h * 0.73f, w * 0.24f, h * 0.78f, w * 0.34f, h * 0.84f)
            cubicTo(w * 0.46f, h * 0.9f, w * 0.58f, h * 0.72f, w * 0.7f, h * 0.8f)
            cubicTo(w * 0.82f, h * 0.88f, w * 0.9f, h * 0.76f, w, h * 0.82f)
            lineTo(w, h)
            close()
        }

        val frontPath = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.9f)
            cubicTo(w * 0.14f, h * 0.82f, w * 0.26f, h * 0.98f, w * 0.38f, h * 0.9f)
            cubicTo(w * 0.5f, h * 0.8f, w * 0.62f, h * 0.96f, w * 0.76f, h * 0.89f)
            cubicTo(w * 0.87f, h * 0.83f, w * 0.94f, h * 0.92f, w, h * 0.88f)
            lineTo(w, h)
            close()
        }

        canvas.drawPath(backPath, hillBackPaint)
        canvas.drawPath(frontPath, hillFrontPaint)
    }

    private fun drawCloudBand(canvas: Canvas, w: Float, h: Float, drift: Float) {
        drawCloud(canvas, w * 0.14f + drift * 10.dp, h * 0.3f, 26.dp)
        drawCloud(canvas, w * 0.56f - drift * 12.dp, h * 0.18f, 22.dp)
        drawCloud(canvas, w * 0.68f + drift * 6.dp, h * 0.46f, 30.dp)
    }

    private fun drawCloud(canvas: Canvas, x: Float, y: Float, size: Float) {
        canvas.drawCircle(x, y, size, cloudPaint)
        canvas.drawCircle(x + size * 0.8f, y - size * 0.28f, size * 0.72f, cloudPaint)
        canvas.drawCircle(x + size * 1.45f, y, size * 0.58f, cloudPaint)
        canvas.drawRoundRect(
            x - size * 0.18f,
            y,
            x + size * 1.82f,
            y + size * 0.56f,
            size,
            size,
            cloudPaint
        )
    }

    private data class StarSpec(
        val x: Float,
        val y: Float,
        val size: Float,
        val offset: Float
    )

    private val Int.dp: Float
        get() = this * resources.displayMetrics.density

    private val Float.dp: Float
        get() = this * resources.displayMetrics.density
}