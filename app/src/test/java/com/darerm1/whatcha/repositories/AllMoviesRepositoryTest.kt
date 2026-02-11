package com.darerm1.whatcha.repositories

import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class AllMoviesRepositoryTest {
    private lateinit var repository: AllMoviesRepositoryImpl

    @Before
    fun setUp() {
        repository = AllMoviesRepositoryImpl()
    }

    @Test
    fun `searchMovies should return matching movies`() {
        val result = repository.searchMovies("Interstellar")

        assertEquals(1, result.size)
        assertEquals("Interstellar", result[0].name)
    }

    @Test
    fun `searchMovies should be case insensitive`() {
        val result = repository.searchMovies("interstellar")

        assertEquals(1, result.size)
    }

    @Test
    fun `searchMovies should return empty list for no match`() {
        val result = repository.searchMovies("zzz")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getMovieById should return correct movie`() {
        val result = repository.getMovieById(1L)

        assertNotNull(result)
        assertEquals("Interstellar", result?.name)
    }

    @Test
    fun `getMovieById should return null for non-existent id`() {
        val result = repository.getMovieById(999L)

        assertNull(result)
    }
}