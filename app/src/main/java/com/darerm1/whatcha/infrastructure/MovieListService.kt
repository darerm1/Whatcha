package com.darerm1.whatcha.infrastructure

import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.data.enums.Status
import com.darerm1.whatcha.data.interfaces.MovieListRepository
import com.darerm1.whatcha.utils.Result

class MovieListService(val movieListRepo: MovieListRepository) {
    fun addMovie(movie: MediaItem): Result<Unit> {
        if (movieListRepo.findMovieById(movie.id) == null) {
            movieListRepo.addMovie(movie)
            return Result.Success(Unit)
        }
        return Result.Error("Movie already in the list")
    }

    fun removeMovieById(id: Long): Result<Unit> {
        if (movieListRepo.findMovieById(id) != null) {
            movieListRepo.removeMovieById(id)
            return Result.Success(Unit)
        }
        return Result.Error("The movie is already not in the list")
    }

    fun markAsCompleted(id: Long) { 
        return movieListRepo.changeStatus(id, Status.COMPLETED)
    }

    fun markAsPlanned(id: Long) { 
        return movieListRepo.changeStatus(id, Status.PLANNED)
    }

    fun markAsAbandoned(id: Long) { 
        return movieListRepo.changeStatus(id, Status.ABANDONED)
    }

    fun markAsNotSet(id: Long) {
        return movieListRepo.changeStatus(id, Status.NOT_SET)
    }

    fun searchMoviesByName(query: String): List<MediaItem> { 
        return movieListRepo.searchMoviesByName(query)
    }

    fun getMoviesByStatus(status: Status): List<MediaItem> { 
        return movieListRepo.getMoviesByStatus(status)
    }

    fun getMoviesSortedByYear(ascending: Boolean = true): List<MediaItem> { 
        return movieListRepo.getMoviesSortedByYearOrRating(ascending, "year")
    }

    fun getMoviesSortedByRating(ascending: Boolean = true): List<MediaItem> { 
        return movieListRepo.getMoviesSortedByYearOrRating(ascending, "rating")
    }

    fun getStatistics(): String { 
        return movieListRepo.getStatistics()
    }

    fun updateRating(id: Long, newRating: Int): Result<Unit> {
        if (newRating !in 1..10) return Result.Error("Rating must be between 1 and  10")
        if (movieListRepo.findMovieById(id) == null) return Result.Error("Movie not found")
        return if (movieListRepo.updateRating(id, newRating)) Result.Success(Unit) else Result.Error("Failed to update rating")
    }

    fun getMovies(): List<MediaItem> {
        return movieListRepo.getMovies()
    }
}