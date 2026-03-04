package com.darerm1.whatcha.data.interfaces

import com.darerm1.whatcha.data.common.NetworkResult

interface AllMoviesRepository {
    suspend fun searchMovies(query: String, limit: Int = 20): NetworkResult<List<MediaItem>>
    suspend fun loadMore(): NetworkResult<List<MediaItem>>
    suspend fun getMovieById(id: Long): NetworkResult<MediaItem>
    fun clearCache()
    fun hasMoreData(): Boolean
}