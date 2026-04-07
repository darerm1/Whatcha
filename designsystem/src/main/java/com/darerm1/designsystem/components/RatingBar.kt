package com.darerm1.designsystem.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.darerm1.designsystem.R
import com.darerm1.designsystem.databinding.ComponentRatingBarBinding

class RatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ComponentRatingBarBinding.inflate(LayoutInflater.from(context), this, true)

    var rating: Float = 0f
        set(value) {
            field = value.coerceIn(0f, 10f)
            binding.ratingText.text = "${field.toInt()}"
        }

    var editable: Boolean = true
        set(value) {
            field = value
            binding.ratingBar.isEnabled = value
        }

    var onRatingChangeListener: ((Float) -> Unit)? = null

    init {
        binding.ratingBar.onRatingChange = { newRating ->
            rating = newRating
            onRatingChangeListener?.invoke(rating)
        }
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.RatingBar)
            rating = a.getFloat(R.styleable.RatingBar_rating, 0f)
            editable = a.getBoolean(R.styleable.RatingBar_ratingEditable, true)
            a.recycle()
        }
    }
}