package com.darerm1.whatcha.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.withClip
import com.darerm1.whatcha.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

class StarRatingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onRatingChange: ((Float) -> Unit)? = null

    var rating: Float = 0f
        set(value) {
            val normalized = normalize(value)
            if (field == normalized) return
            field = normalized
            invalidate()
            onRatingChange?.invoke(field)
        }

    private val filledStar: Drawable = requireNotNull(
        AppCompatResources.getDrawable(context, R.drawable.ic_star_filled)
    )

    private val outlineStar: Drawable = requireNotNull(
        AppCompatResources.getDrawable(context, R.drawable.ic_star_outline)
    )

    private val tempRect = Rect()
    private val starCount = 10
    private val starSpacing = 4.dp
    private val desiredHeight = 36.dp

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = paddingLeft + paddingRight + (desiredHeight * starCount) + (starSpacing * (starCount - 1))
        val measuredWidth = resolveSize(desiredWidth, widthMeasureSpec)
        val measuredHeight = resolveSize(paddingTop + paddingBottom + desiredHeight, heightMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val availableWidth = width - paddingLeft - paddingRight
        val availableHeight = height - paddingTop - paddingBottom
        if (availableWidth <= 0 || availableHeight <= 0) return

        val starSize = min(
            (availableWidth - starSpacing * (starCount - 1)).toFloat() / starCount,
            availableHeight.toFloat()
        )
        if (starSize <= 0f) return

        val contentWidth = starSize * starCount + starSpacing * (starCount - 1)
        val startX = paddingLeft + ((availableWidth - contentWidth) / 2f)
        val top = paddingTop + ((availableHeight - starSize) / 2f)

        repeat(starCount) { index ->
            val left = (startX + index * (starSize + starSpacing)).toInt()
            val right = (left + starSize).toInt()
            val bottom = (top + starSize).toInt()
            val fillFraction = (rating - index).coerceIn(0f, 1f)

            tempRect.set(left, top.toInt(), right, bottom)
            outlineStar.bounds = tempRect
            outlineStar.draw(canvas)

            if (fillFraction > 0f) {
                filledStar.bounds = tempRect
                canvas.withClip(left, top.toInt(), (left + starSize * fillFraction).toInt(), bottom) {
                    filledStar.draw(this)
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE,
            MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                rating = touchToRating(event.x)
                if (event.actionMasked == MotionEvent.ACTION_UP) {
                    performClick()
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    private fun touchToRating(touchX: Float): Float {
        val availableWidth = max(1, width - paddingLeft - paddingRight)
        val relative = ((touchX - paddingLeft) / availableWidth).coerceIn(0f, 1f)
        return relative * starCount
    }

    private fun normalize(value: Float): Float {
        val clamped = value.coerceIn(0f, starCount.toFloat())
        return round(clamped * 10f) / 10f
    }

    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()
}
