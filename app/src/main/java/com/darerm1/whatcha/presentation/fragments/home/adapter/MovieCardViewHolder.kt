package com.darerm1.whatcha.presentation.fragments.home.adapter

import android.view.View
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.darerm1.designsystem.components.MovieCard
import com.darerm1.whatcha.R
import com.darerm1.whatcha.domain.entities.MediaItem

class MovieCardViewHolder(
    private val movieCard: MovieCard,
    private val onFavoriteClick: (MediaItem) -> Unit,
    private val onItemClick: (MediaItem) -> Unit,
    private val isFavorite: (Long) -> Boolean
) : RecyclerView.ViewHolder(movieCard) {

    private var currentMovie: MediaItem? = null

    init {
        movieCard.setOnClickListener {
            currentMovie?.let { onItemClick(it) }
        }
        movieCard.onFavoriteClickListener = { _ ->
            currentMovie?.let { onFavoriteClick(it) }
        }
    }

    fun bind(movie: MediaItem) {
        currentMovie = movie
        movieCard.title = movie.name
        movieCard.yearGenre = "${movie.year}, ${movie.genre.russianName}"
        movieCard.posterUrl = movie.posterUrl
        movieCard.isFavorite = isFavorite(movie.id)
    }
}

class LoadMoreItemViewHolder(itemView: View, onLoadMoreClick: () -> Unit) : RecyclerView.ViewHolder(itemView) {
    init {
        val button = itemView.findViewById<com.darerm1.designsystem.components.PrimaryButton>(R.id.btnLoadMore)
        button.setOnClickListener { onLoadMoreClick() }
    }
}

class MovieCardDiffCallback : DiffUtil.ItemCallback<MediaItem>() {
    override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean = oldItem.contentEquals(newItem)
}