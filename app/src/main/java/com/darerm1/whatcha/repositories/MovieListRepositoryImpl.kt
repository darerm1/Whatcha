package com.darerm1.whatcha.repositories

import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.data.interfaces.MovieListRepository
import com.darerm1.whatcha.data.enums.Status

class MovieListRepositoryImpl: MovieListRepository {
    private val movieList = mutableListOf<MediaItem>()

    override fun addMovie(movie: MediaItem) {
        movieList.add(movie)
    }

    override fun removeMovieById(id: Long) {
        movieList.removeIf { a -> a.id == id }
    }

    override fun changeStatus(id: Long, newStatus: Status) {
        val movie = movieList.find { a -> a.id == id }
        movie?.status = newStatus
    }

    override fun searchMoviesByName(query: String): List<MediaItem> {
        return movieList.filter { a -> a.name.contains(query, ignoreCase = true) }
    }

    override fun findMovieById(id: Long): MediaItem? {
        return movieList.find { a -> a.id == id }
    }

    override fun getMoviesByStatus(status: Status): List<MediaItem> {
        return movieList.filter { a -> a.status  == status}
    }

    override fun getMoviesSortedByYearOrRating(ascending: Boolean, comparator: String): List<MediaItem> {
        return if (ascending) {
            if (comparator == "year")
                movieList.sortedBy { a -> a.year }
            else
                movieList.sortedBy { a -> a.personalRating }
        } else {
            if (comparator == "year")
                movieList.sortedByDescending { a -> a.year }
            else
                movieList.sortedByDescending { a -> a.personalRating }
        }
    }

    override fun getStatistics(): String {
        val total = movieList.size
        val completed = movieList.count { a -> a.status == Status.COMPLETED }
        val avgRating = movieList
            .filter { a -> a.personalRating != null }
            .mapNotNull { a -> a.personalRating }
            .average()
            .takeIf { a -> !a.isNaN() } ?: 0.0

        return "Movie statistics:" +
                "Total movies in list: " + total +
                "Completed movies: " + completed +
                "Average rating: " + avgRating
    }

    override fun updateRating(id: Long, newRating: Int): Boolean {
        val movie = movieList.find { a -> a.id == id }
        movie?.personalRating = newRating
        if (movie != null && movie.status != Status.COMPLETED)
            movie.status = Status.COMPLETED
        return movie != null
    }

    override fun getMovies(): List<MediaItem> {
        return movieList
    }
}