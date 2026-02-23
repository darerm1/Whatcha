package com.darerm1.whatcha.data.interfaces

import com.darerm1.whatcha.data.enums.Status
import java.time.LocalDate

interface MovieListRepository {
    fun addMovie(movie: MediaItem)

    fun removeMovieById(id: Long)

    fun changeStatus(id: Long, newStatus: Status)

    fun searchMoviesByName(query: String): List<MediaItem>

    fun findMovieById(id: Long): MediaItem?

    fun getMoviesByStatus(status: Status): List<MediaItem>

    fun getMoviesSortedByYearOrRating(ascending: Boolean = true, comparator: String = "rating"): List<MediaItem>

    fun updateRating(id: Long, newRating: Int): Boolean

    fun getMovies(): List<MediaItem>

    fun getStatistics(): String

    fun changeDate(id: Long, newDate: LocalDate)
}