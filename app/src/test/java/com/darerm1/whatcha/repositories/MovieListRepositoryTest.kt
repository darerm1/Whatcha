package com.darerm1.whatcha.repositories

import com.darerm1.whatcha.data.enums.Genre
import com.darerm1.whatcha.data.enums.Status
import com.darerm1.whatcha.data.models.Movie
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class MovieListRepositoryTest {
    private lateinit var repository: MovieListRepositoryImpl

    @Before
    fun setUp() {
        repository = MovieListRepositoryImpl()
    }

    @Test
    fun `addMovie should increase movie count`() {
        val initialCount = repository.getMovies().size
        val movie = Movie(
            id = 1, name = "...", year = 2014, genre = Genre.FANTASY,
            duration = 169, personalRating = 9, status = Status.COMPLETED,
            description = "...", trailerUrl = "...")

        repository.addMovie(movie)

        assertEquals(initialCount + 1, repository.getMovies().size)
    }

    @Test
    fun `removeMovieById should decrease movie count`() {
        val movie = Movie(
            id = 1, name = "...", year = 2014, genre = Genre.FANTASY,
            duration = 169, personalRating = 9, status = Status.COMPLETED,
            description = "...", trailerUrl = "...")
        repository.addMovie(movie)
        val initialCount = repository.getMovies().size

        repository.removeMovieById(1)

        assertEquals(initialCount - 1, repository.getMovies().size)
    }

    @Test
    fun `updateRating should change movie rating`() {
        val movie = Movie(
            id = 1, name = "...", year = 2014, genre = Genre.FANTASY,
            duration = 169, personalRating = 9, status = Status.COMPLETED,
            description = "...", trailerUrl = "...")
        repository.addMovie(movie)

        repository.updateRating(1, 9)

        val updatedMovie = repository.findMovieById(1)
        assertEquals(9, updatedMovie?.personalRating)
    }

    @Test
    fun `getMoviesSortedByYear should return sorted list`() {
        val movie1 = Movie(
            id = 1, name = "Old", year = 2000, genre = Genre.FANTASY,
            duration = 169, personalRating = 9, status = Status.COMPLETED,
            description = "...", trailerUrl = "...")
        val movie2 = Movie(
            id = 2, name = "New", year = 2020, genre = Genre.FANTASY,
            duration = 169, personalRating = 9, status = Status.COMPLETED,
            description = "...", trailerUrl = "...")
        repository.addMovie(movie2)
        repository.addMovie(movie1)

        val sorted = repository.getMoviesSortedByYearOrRating(ascending = true, comparator = "year")

        assertEquals(2000, sorted[0].year)
        assertEquals(2020, sorted[1].year)
    }
}