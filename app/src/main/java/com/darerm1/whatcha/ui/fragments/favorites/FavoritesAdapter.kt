package com.darerm1.whatcha.ui.fragments.favorites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darerm1.whatcha.R
import com.darerm1.whatcha.data.enums.Status
import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.utils.RatingFormatter

class FavoritesAdapter(
    private var movies: List<MediaItem>,
    private val onItemClick: (MediaItem) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.MovieViewHolder>() {

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleText: TextView = itemView.findViewById(R.id.movie_title)
        private val yearGenreText: TextView = itemView.findViewById(R.id.movie_year_genre)
        private val ratingText: TextView = itemView.findViewById(R.id.movie_rating)
        private val dateStatusText: TextView = itemView.findViewById(R.id.movie_date_status)

        fun bind(movie: MediaItem, clickListener: (MediaItem) -> Unit) {
            titleText.text = movie.name
            yearGenreText.text = "${movie.year}, ${movie.genre}"

            ratingText.text = movie.personalRating?.let {
                "Мой: ${RatingFormatter.formatPersonalRating(it)}/10"
            } ?: "Не оценен"

            val dateText = if (movie.date != null) {
                "Просмотрено: ${movie.date}"
            } else {
                "Не просмотрено"
            }

            val statusText = when (movie.status) {
                Status.PLANNED -> itemView.context.getString(R.string.status_planned)
                Status.COMPLETED -> itemView.context.getString(R.string.status_completed)
                Status.ABANDONED -> itemView.context.getString(R.string.status_abandoned)
                Status.NOT_SET -> itemView.context.getString(R.string.status_not_set)
            }

            dateStatusText.text = "$dateText, Статус: $statusText"
            itemView.setOnClickListener { clickListener(movie) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_favorite_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(movies[position], onItemClick)
    }

    override fun getItemCount() = movies.size

    fun updateList(newMovies: List<MediaItem>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}
