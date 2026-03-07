package com.darerm1.whatcha.ui.fragments.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.darerm1.whatcha.R
import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.databinding.ItemLoadMoreBinding
import com.darerm1.whatcha.databinding.ItemMovieBinding

class MovieAdapter(
    private val onFavoriteClick: (MediaItem) -> Unit,
    private val onItemClick: (MediaItem) -> Unit,
    private val isFavorite: (Long) -> Boolean,
    private val onLoadMoreClick: () -> Unit
) : ListAdapter<ListItem, RecyclerView.ViewHolder>(ListItemDiffCallback()) {

    companion object {
        private const val VIEW_TYPE_MOVIE = 0
        private const val VIEW_TYPE_LOAD_MORE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ListItem.MovieItem -> VIEW_TYPE_MOVIE
            is ListItem.LoadMoreItem -> VIEW_TYPE_LOAD_MORE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_MOVIE -> MovieViewHolder.create(parent, onFavoriteClick, onItemClick, isFavorite)
            VIEW_TYPE_LOAD_MORE -> LoadMoreViewHolder.create(parent, onLoadMoreClick)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is ListItem.MovieItem -> (holder as MovieViewHolder).bind(item.movie)
            is ListItem.LoadMoreItem -> { }
        }
    }
}

class LoadMoreViewHolder(
    private val binding: ItemLoadMoreBinding,
    onLoadMoreClick: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.btnLoadMore.setOnClickListener { onLoadMoreClick() }
    }

    companion object {
        fun create(parent: ViewGroup, onLoadMoreClick: () -> Unit): LoadMoreViewHolder {
            val binding = ItemLoadMoreBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return LoadMoreViewHolder(binding, onLoadMoreClick)
        }
    }
}

class ListItemDiffCallback : DiffUtil.ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return when {
            oldItem is ListItem.MovieItem && newItem is ListItem.MovieItem -> oldItem.movie.id == newItem.movie.id
            oldItem is ListItem.LoadMoreItem && newItem is ListItem.LoadMoreItem -> true
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
        return when {
            oldItem is ListItem.MovieItem && newItem is ListItem.MovieItem -> oldItem.movie.contentEquals(newItem.movie)
            oldItem is ListItem.LoadMoreItem && newItem is ListItem.LoadMoreItem -> true
            else -> false
        }
    }
}
