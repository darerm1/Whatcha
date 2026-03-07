package com.darerm1.whatcha.ui.fragments.favorites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.darerm1.whatcha.R
import com.darerm1.whatcha.data.enums.Status
import com.darerm1.whatcha.data.interfaces.MediaItem

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
            var message: String = "${movie.year}, ${movie.genre}"
            yearGenreText.text = message
            
            val kpText = if (movie.kpRating != null) {
                "Кинопоиск: ${String.format("%.1f", movie.kpRating)}"
            } else {
                ""
            }
            
            val personalText = if (movie.personalRating != null) {
                "Мой: ${movie.personalRating}/10"
            } else {
                "Не оценен"
            }
            
            ratingText.text = if (kpText.isNotEmpty()) {
                "$kpText | $personalText"
            } else {
                personalText
            }

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