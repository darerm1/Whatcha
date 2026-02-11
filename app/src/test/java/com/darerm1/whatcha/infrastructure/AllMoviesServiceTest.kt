package com.darerm1.whatcha.infrastructure

import com.darerm1.whatcha.data.interfaces.AllMoviesRepository
import com.darerm1.whatcha.data.interfaces.MediaItem
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import io.mockk.*

class AllMoviesServiceTest {
    private lateinit var service: AllMoviesService

    private lateinit var mockRepository: AllMoviesRepository

    @Before
    fun setUp() {
        mockRepository = mockk()
        service = AllMoviesService(mockRepository)
    }

    @Test
    fun `searchMovies should call repository searchMovies`() {
        val movies = listOf<MediaItem>(mockk(), mockk())
        every { mockRepository.searchMovies("query") } returns movies

        val result = service.searchMovies("query")

        assertEquals(2, result.size)
        verify { mockRepository.searchMovies("query") }
    }

    @Test
    fun `getMovieById should call repository getMovieById`() {
        val movie: MediaItem = mockk()
        every { mockRepository.getMovieById(1L) } returns movie

        val result = service.getMovieById(1L)

        assertSame(movie, result)
        verify { mockRepository.getMovieById(1L) }
    }

    @Test
    fun `getMovieById should return null when repository returns null`() {
        every { mockRepository.getMovieById(1L) } returns null

        val result = service.getMovieById(1L)

        assertNull(result)
    }
}