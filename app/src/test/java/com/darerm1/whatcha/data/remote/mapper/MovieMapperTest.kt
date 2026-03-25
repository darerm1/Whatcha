package com.darerm1.whatcha.data.remote.mapper

import com.darerm1.whatcha.data.enums.Genre
import com.darerm1.whatcha.data.models.MovieRatings
import com.darerm1.whatcha.data.remote.dto.*
import com.darerm1.whatcha.utils.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MovieMapperTest {

    @Test
    fun `mapDtoToDomain should map all fields correctly`() {
        val dto = MovieDto(
            id = 1,
            name = "Test Movie",
            alternativeName = null,
            names = null,
            year = 2020,
            description = "Test description",
            genres = listOf(GenreDto("Ð´Ñ€Ð°Ð¼Ð°")),
            poster = PosterDto("http://example.com/poster.jpg", null),
            movieLength = 120,
            rating = RatingDto(kp = 8.5, imdb = 8.0, tmdb = 7.2, filmCritics = 7.5, russianFilmCritics = 8.0, await = 9.0)
        )

        val result = MovieMapper.mapDtoToDomain(dto)

        assertTrue(result is Result.Success)
        val movie = (result as Result.Success).data
        assertEquals(1L, movie.id)
        assertEquals("Test Movie", movie.name)
        assertEquals(2020, movie.year)
        assertEquals(Genre.DRAMA, movie.genre)
        assertEquals("Test description", movie.description)
        assertEquals("http://example.com/poster.jpg", movie.posterUrl)
        assertEquals(120, movie.duration)
        assertNull(movie.personalRating)
        assertEquals(8.5, movie.kpRating!!, 0.001)
        assertEquals(
            MovieRatings(
                kp = 8.5,
                imdb = 8.0,
                tmdb = 7.2,
                filmCritics = 7.5,
                russianFilmCritics = 8.0,
                await = 9.0
            ),
            movie.ratings
        )
    }

    @Test
    fun `mapDtoToDomain should normalize zero ratings to null`() {
        val dto = MovieDto(
            id = 3,
            name = "No Ratings Yet",
            alternativeName = null,
            names = null,
            year = 2024,
            description = "Test",
            genres = listOf(GenreDto("Ð´Ñ€Ð°Ð¼Ð°")),
            poster = null,
            movieLength = 100,
            rating = RatingDto(kp = 0.0, imdb = 0.0, tmdb = 0.0, filmCritics = 0.0, russianFilmCritics = 0.0, await = 0.0)
        )

        val result = MovieMapper.mapDtoToDomain(dto)

        assertTrue(result is Result.Success)
        val movie = (result as Result.Success).data
        assertNull(movie.kpRating)
        assertNull(movie.ratings)
    }

    @Test
    fun `mapDtoToDomain should use default genre for unknown genre`() {
        val dto = MovieDto(
            id = 1,
            name = "Test",
            alternativeName = null,
            names = null,
            year = 2020,
            description = "Test",
            genres = listOf(GenreDto("Ð½ÐµÐ¸Ð·Ð²ÐµÑÑ‚Ð½Ñ‹Ð¹ Ð¶Ð°Ð½Ñ€")),
            poster = null,
            movieLength = null,
            rating = null
        )

        val result = MovieMapper.mapDtoToDomain(dto)

        assertTrue(result is Result.Success)
        val movie = (result as Result.Success).data
        assertEquals(Genre.DRAMA, movie.genre)
    }

    @Test
    fun `mapDtoToDomain should handle null fields`() {
        val dto = MovieDto(
            id = 2,
            name = null,
            alternativeName = null,
            names = null,
            year = null,
            description = null,
            genres = listOf(GenreDto("ÐºÐ¾Ð¼ÐµÐ´Ð¸Ñ")),
            poster = null,
            movieLength = null,
            rating = null
        )

        val result = MovieMapper.mapDtoToDomain(dto)

        assertTrue(result is Result.Success)
        val movie = (result as Result.Success).data
        assertEquals("Unknown", movie.name)
        assertEquals(0, movie.year)
        assertEquals("", movie.description)
        assertNull(movie.posterUrl)
        assertEquals(0, movie.duration)
        assertNull(movie.personalRating)
        assertNull(movie.ratings)
    }
}
