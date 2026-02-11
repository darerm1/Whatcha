package com.darerm1.whatcha.infrastructure

import com.darerm1.whatcha.data.enums.Genre
import com.darerm1.whatcha.data.enums.Status
import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.data.interfaces.MovieListRepository
import com.darerm1.whatcha.data.models.Movie
import com.darerm1.whatcha.repositories.MovieListRepositoryImpl
import com.darerm1.whatcha.utils.Result
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class MovieListServiceTest {
    private lateinit var service: MovieListService

    private lateinit var mockRepository: MovieListRepository

    @Before
    fun setUp() {
        mockRepository = mockk()
        service = MovieListService(mockRepository)
    }

    @Test
    fun `addMovie should call repository addMovie`() {
        val movie: MediaItem = mockk()
        every { mockRepository.addMovie(movie) } just runs
        every { movie.id } returns 1L
        every { mockRepository.findMovieById(1L) } returns null

        service.addMovie(movie)

        verify(exactly = 1) { mockRepository.addMovie(movie) }
    }

    @Test
    fun `addMovie with valid movie should return success`() {
        val movie: MediaItem = mockk()

        every { movie.id } returns 1L
        every { mockRepository.findMovieById(1L) } returns null
        every { mockRepository.addMovie(movie) } just runs

        val result = service.addMovie(movie)

        assertTrue(result is Result.Success)
    }

    @Test
    fun `updateRating should return success for valid rating`() {
        val movieId = 1L
        val rating = 8
        val movie: MediaItem = mockk()
        every { mockRepository.updateRating(movieId, rating) } returns true
        every { mockRepository.findMovieById(movieId) } returns movie

        val result = service.updateRating(movieId, rating)

        assertTrue(result is Result.Success)
    }

    @Test
    fun `updateRating should return error for invalid rating`() {
        val movieId = 1L
        val rating = 15
        val movie: MediaItem = mockk()
        every { mockRepository.findMovieById(movieId) } returns movie

        val result = service.updateRating(movieId, rating)

        assertTrue(result is Result.Error)
    }

    @Test
    fun `markAsCompleted should update status to COMPLETED`() {
        val movieId = 1L
        every { mockRepository.changeStatus(movieId, Status.COMPLETED) } just runs

        service.markAsCompleted(movieId)

        verify { mockRepository.changeStatus(movieId, Status.COMPLETED) }
    }

    @Test
    fun `getStatistics should calculate correctly`() {
        val movie1 = Movie(
            id = 1, name = "...", year = 2014, genre = Genre.FANTASY,
            duration = 169, personalRating = 9, status = Status.COMPLETED,
            description = "...", trailerUrl = "...")

        val movie2 = Movie(
            id = 2, name = "...", year = 2010, genre = Genre.THRILLER,
            duration = 148, personalRating = 8, status = Status.COMPLETED,
            description = "...", trailerUrl = "...")

        val movie3 = Movie(
            id = 3, name = "...", year = 2021, genre = Genre.FANTASY,
            duration = 155, personalRating = null, status = Status.PLANNED,
            description = "...", trailerUrl = "...")

        val movie4 = Movie(
            id = 4, name = "...", year = 2020, genre = Genre.THRILLER,
            duration = 150, personalRating = 7, status = Status.ABANDONED,
            description = "...", trailerUrl = "...")

        val repository = MovieListRepositoryImpl()
        repository.addMovie(movie1)
        repository.addMovie(movie2)
        repository.addMovie(movie3)
        repository.addMovie(movie4)
        val service = MovieListService(repository)

        val statistics = service.getStatistics()

        assertTrue(statistics.contains("Total movies in list: 4"))
        assertTrue(statistics.contains("Completed movies: 2"))
        assertTrue(statistics.contains("Average rating: 8.0"))
    }

    @Test
    fun `searchMovies should return filtered list`() {
        val movie1 = Movie(
            id = 1, name = "Interstellar", year = 2014, genre = Genre.DRAMA,
            duration = 169, personalRating = 9, status = Status.COMPLETED,
            description = "...", trailerUrl = "...")

        val movie2 = Movie(
            id = 2, name = "Inception", year = 2010, genre = Genre.THRILLER,
            duration = 148, personalRating = 8, status = Status.COMPLETED,
            description = "...", trailerUrl = "...")

        val movie3 = Movie(
            id = 3, name = "Dune", year = 2021, genre = Genre.FANTASY,
            duration = 155, personalRating = null, status = Status.PLANNED,
            description = "...", trailerUrl = "...")

        val movie4 = Movie(
            id = 4, name = "Tenet", year = 2020, genre = Genre.THRILLER,
            duration = 150, personalRating = 7, status = Status.ABANDONED,
            description = "...", trailerUrl = "...")

        val repository = MovieListRepositoryImpl()
        repository.addMovie(movie1)
        repository.addMovie(movie2)
        repository.addMovie(movie3)
        repository.addMovie(movie4)

        val service = MovieListService(repository)

        val result1 = service.searchMoviesByName("Interstellar")
        assertEquals(1, result1.size)
        assertEquals("Interstellar", result1[0].name)

        val result2 = service.searchMoviesByName("stell")
        assertEquals(1, result2.size)
        assertEquals("Interstellar", result2[0].name)

        val result3 = service.searchMoviesByName("INTERSTELLAR")
        assertEquals(1, result3.size)

        val result4 = service.searchMoviesByName("in")
        assertEquals(2, result4.size)
        assertTrue(result4.any { it.name == "Inception" })
        assertTrue(result4.any { it.name == "Interstellar" })

        val result5 = service.searchMoviesByName("zzz")
        assertEquals(0, result5.size)
        assertTrue(result5.isEmpty())
    }
}