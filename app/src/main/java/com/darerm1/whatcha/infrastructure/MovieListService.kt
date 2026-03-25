package com.darerm1.whatcha.infrastructure

import com.darerm1.whatcha.data.enums.Status
import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.data.interfaces.MovieListRepository
import com.darerm1.whatcha.repositories.MovieListRepositoryImpl
import com.darerm1.whatcha.utils.Result
import java.time.LocalDate

class MovieListService(private val movieListRepo: MovieListRepository) {

    companion object {
        val instance: MovieListService by lazy { MovieListService(MovieListRepositoryImpl.instance) }
    }

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
        movieListRepo.changeDate(id, LocalDate.now())
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

    fun updateRating(id: Long, newRating: Float): Result<Unit> {
        if (newRating <= 0f || newRating > 10f) return Result.Error("Rating must be between 0.1 and 10")
        if (movieListRepo.findMovieById(id) == null) return Result.Error("Movie not found")
        return if (movieListRepo.updateRating(id, newRating)) Result.Success(Unit) else Result.Error("Failed to update rating")
    }

    fun getMovies(): List<MediaItem> {
        return movieListRepo.getMovies()
    }
}
