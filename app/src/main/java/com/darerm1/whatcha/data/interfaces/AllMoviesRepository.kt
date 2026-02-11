package com.darerm1.whatcha.data.interfaces

interface AllMoviesRepository {
    fun searchMovies(query: String): List<MediaItem>

    fun getMovieById(id: Long): MediaItem?
}