package com.darerm1.whatcha.ui.fragments.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.dispose
import coil.load
import com.darerm1.whatcha.R
import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.databinding.ItemMovieBinding

class MovieViewHolder(
    private val binding: ItemMovieBinding,
    private val onFavoriteClick: (MediaItem) -> Unit,
    private val onItemClick: (MediaItem) -> Unit,
    private val isFavorite: (Long) -> Boolean
) : RecyclerView.ViewHolder(binding.root) {

    private var currentMovie: MediaItem? = null
    private var currentMovieId: Long? = null

    init {
        binding.root.setOnClickListener {
            currentMovie?.let { onItemClick(it) }
        }

        binding.btnFavorite.setOnClickListener {
            currentMovie?.let { onFavoriteClick(it) }
        }
    }

      fun bind(movie: MediaItem) {
          currentMovie = movie
          currentMovieId = movie.id

          binding.tvTitle.text = movie.name
          binding.tvYearGenre.text = "${movie.year}, ${formatGenre(movie.genre.name)}"
          
          binding.tvKpRating.text = if (movie.kpRating != null) {
              "Кинопоиск: ${String.format("%.1f", movie.kpRating)}"
          } else {
              ""
          }

          // Cancel previous image loading request
          binding.ivPoster.dispose()
          
          val posterUrl = movie.posterUrl
          if (posterUrl.isNullOrBlank()) {
              binding.ivPoster.setImageResource(R.drawable.placeholder_poster)
              binding.tvPlaceholder.visibility = android.view.View.GONE
          } else {
              binding.tvPlaceholder.visibility = android.view.View.GONE
              
              binding.ivPoster.load(posterUrl) {
                  crossfade(true)
                  placeholder(R.drawable.placeholder_poster)
                  error(R.drawable.placeholder_poster)
                  listener(
                      onSuccess = { _, _ ->
                          // Check if ViewHolder is still showing this movie
                          if (currentMovieId == movie.id) {
                              android.util.Log.d("MovieViewHolder", "Poster loaded: ${movie.name}")
                              binding.tvPlaceholder.visibility = android.view.View.GONE
                          }
                      },
                      onError = { _, error ->
                          if (currentMovieId == movie.id) {
                              android.util.Log.e("MovieViewHolder", "Failed to load poster for ${movie.name}: ${error.throwable?.message}")
                          }
                      }
                  )
              }
          }

          updateFavoriteIcon(movie.id)
      }

    private fun updateFavoriteIcon(movieId: Long) {
        val isFav = isFavorite(movieId)
        if (isFav) {
            binding.btnFavorite.setImageResource(R.drawable.ic_favorite)
            binding.btnFavorite.visibility = View.VISIBLE
        } else {
            binding.btnFavorite.visibility = View.GONE
        }
    }

    private fun formatGenre(genre: String): String {
        return genre.lowercase().replaceFirstChar { it.uppercase() }
    }

    companion object {
        fun create(
            parent: ViewGroup,
            onFavoriteClick: (MediaItem) -> Unit,
            onItemClick: (MediaItem) -> Unit,
            isFavorite: (Long) -> Boolean
        ): MovieViewHolder {
            val binding = ItemMovieBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return MovieViewHolder(binding, onFavoriteClick, onItemClick, isFavorite)
        }
    }
}
