package com.darerm1.whatcha.presentation.fragments.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.darerm1.designsystem.components.MovieCard
import com.darerm1.whatcha.domain.entities.MediaItem
import com.darerm1.whatcha.R

class MovieCardAdapter(
    private val onFavoriteClick: (MediaItem) -> Unit,
    private val onItemClick: (MediaItem) -> Unit,
    private val isFavorite: (Long) -> Boolean,
    private val onLoadMoreClick: () -> Unit
) : ListAdapter<MediaItem, RecyclerView.ViewHolder>(MovieCardDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_MOVIE = 0
        private const val VIEW_TYPE_LOAD_MORE = 1
    }

    private var showLoadMore = false
    private var pendingShowLoadMore = false

    override fun getItemViewType(position: Int): Int {
        return if (isLoadMorePosition(position)) VIEW_TYPE_LOAD_MORE else VIEW_TYPE_MOVIE
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (showLoadMore) 1 else 0
    }

    override fun getItemId(position: Int): Long {
        return if (isLoadMorePosition(position)) {
            Long.MAX_VALUE
        } else {
            getItem(position).id
        }
    }

    fun submitMovieList(movies: List<MediaItem>, hasMore: Boolean) {
        pendingShowLoadMore = hasMore
        submitList(movies) {
            val previous = showLoadMore
            showLoadMore = pendingShowLoadMore
            if (previous != showLoadMore) {
                val itemCountWithoutButton = super.getItemCount()
                if (showLoadMore) {
                    notifyItemInserted(itemCountWithoutButton)
                } else {
                    notifyItemRemoved(itemCountWithoutButton)
                }
            }
        }
    }

    fun isLoadMorePosition(position: Int): Boolean {
        return showLoadMore && position == super.getItemCount()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_MOVIE -> MovieCardViewHolder(
                MovieCard(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    placeholderRes = R.drawable.poster_placeholder_branded
                },
                onFavoriteClick,
                onItemClick,
                isFavorite
            )
            else -> LoadMoreItemViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_load_more, parent, false),
                onLoadMoreClick
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is MovieCardViewHolder -> {
                val movie = getItem(position)
                holder.bind(movie)
            }
            is LoadMoreItemViewHolder -> { }
        }
    }
}
