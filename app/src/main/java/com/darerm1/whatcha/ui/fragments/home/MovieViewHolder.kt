package com.darerm1.whatcha.ui.fragments.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.darerm1.whatcha.R
import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.databinding.ItemMovieBinding

class MovieViewHolder(
    private val binding: ItemMovieBinding,
    private val onFavoriteClick: (MediaItem) -> Unit,
    private val isFavorite: (Long) -> Boolean
) : RecyclerView.ViewHolder(binding.root) {

    private var currentMovie: MediaItem? = null

    init {
        binding.btnFavorite.setOnClickListener {
            currentMovie?.let { onFavoriteClick(it) }
        }
    }

    fun bind(movie: MediaItem) {
        currentMovie = movie

        binding.tvTitle.text = movie.name
        binding.tvYearGenre.text = "${movie.year}, ${formatGenre(movie.genre.name)}"

        val posterUrl = movie.posterUrl
        if (posterUrl.isNullOrBlank()) {
            binding.ivPoster.setImageResource(R.drawable.placeholder_poster)
            binding.tvPlaceholder.visibility = android.view.View.VISIBLE
        } else {
            binding.ivPoster.load(posterUrl) {
                placeholder(R.drawable.placeholder_poster)
                error(R.drawable.placeholder_poster)
                listener { _, result ->
                    binding.tvPlaceholder.visibility = 
                        if (result.isSuccess && posterUrl.isNotBlank()) android.view.View.GONE 
                        else android.view.View.VISIBLE
                }
            }
        }

        updateFavoriteIcon(movie.id)
    }

    private fun updateFavoriteIcon(movieId: Long) {
        val isFav = isFavorite(movieId)
        binding.btnFavorite.setImageResource(
            if (isFav) R.drawable.ic_favorite else R.drawable.ic_favorite_border
        )
    }

    private fun formatGenre(genre: String): String {
        return genre.lowercase().replaceFirstChar { it.uppercase() }
    }

    companion object {
        fun create(
            parent: ViewGroup,
            onFavoriteClick: (MediaItem) -> Unit,
            isFavorite: (Long) -> Boolean
        ): MovieViewHolder {
            val binding = ItemMovieBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return MovieViewHolder(binding, onFavoriteClick, isFavorite)
        }
    }
}
