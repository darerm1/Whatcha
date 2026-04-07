package com.darerm1.designsystem.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import coil.dispose
import coil.load
import com.darerm1.designsystem.R
import com.darerm1.designsystem.databinding.ComponentMovieCardBinding

class MovieCard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ComponentMovieCardBinding.inflate(LayoutInflater.from(context), this, true)

    var title: String = ""
        set(value) {
            field = value
            binding.tvTitle.text = value
        }

    var yearGenre: String = ""
        set(value) {
            field = value
            binding.tvYearGenre.text = value
        }

    var posterUrl: String? = null
        set(value) {
            field = value
            binding.ivPoster.dispose()
            if (value.isNullOrBlank()) {
                binding.ivPoster.setImageResource(placeholderRes)
            } else {
                binding.ivPoster.load(value) {
                    placeholder(placeholderRes)
                    error(placeholderRes)
                }
            }
        }

    var isFavorite: Boolean = false
        set(value) {
            field = value
            binding.btnFavorite.setImageResource(
                if (value) R.drawable.ic_favorite_red else R.drawable.ic_favorite_border
            )
        }

    var onFavoriteClickListener: ((Boolean) -> Unit)? = null

    var placeholderRes: Int = R.drawable.ic_movie_placeholder
        set(value) {
            field = value
        }

    init {
        binding.btnFavorite.setOnClickListener {
            isFavorite = !isFavorite
            onFavoriteClickListener?.invoke(isFavorite)
        }

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.MovieCard)
            placeholderRes = typedArray.getResourceId(R.styleable.MovieCard_placeholderDrawable, R.drawable.ic_movie_placeholder)
            title = typedArray.getString(R.styleable.MovieCard_movieTitle) ?: ""
            yearGenre = typedArray.getString(R.styleable.MovieCard_yearGenre) ?: ""
            posterUrl = typedArray.getString(R.styleable.MovieCard_posterUrl)
            isFavorite = typedArray.getBoolean(R.styleable.MovieCard_isFavorite, false)
            typedArray.recycle()
        }
    }
}