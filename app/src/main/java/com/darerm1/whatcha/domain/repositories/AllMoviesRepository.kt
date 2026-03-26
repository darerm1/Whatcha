package com.darerm1.whatcha.domain.repositories

import com.darerm1.whatcha.domain.common.Result
import com.darerm1.whatcha.domain.entities.MediaItem

interface AllMoviesRepository {
    suspend fun searchMovies(query: String, limit: Int = 20): Result<List<MediaItem>>
    suspend fun loadMore(): Result<List<MediaItem>>
    suspend fun getMovieById(id: Long): Result<MediaItem>
    fun clearCache()
    fun hasMoreData(): Boolean
}