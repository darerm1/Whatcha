package com.darerm1.whatcha.data.interfaces

interface AllMoviesRepository {
    fun searchMovies(query: String): List<MediaItem>

    fun searchMovies(query: String, page: Int, pageSize: Int): List<MediaItem>

    fun getMovieById(id: Long): MediaItem?
}