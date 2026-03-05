package com.darerm1.whatcha.data.remote.datasource

import com.darerm1.whatcha.data.remote.SafeApiCall
import com.darerm1.whatcha.data.remote.api.PoiskKinoApi
import com.darerm1.whatcha.data.common.NetworkResult
import com.darerm1.whatcha.data.remote.dto.MovieDto
import com.darerm1.whatcha.data.remote.dto.MovieListResponse

class RemoteMoviesDataSource(
    private val api: PoiskKinoApi
) {
    private val safeApiCall = SafeApiCall()
    
    suspend fun getMovieById(id: Long): NetworkResult<MovieDto> {
        return safeApiCall.execute { api.getMovieById(id) }
    }
    
    suspend fun searchMovies(query: String?, cursor: String?, limit: Int): NetworkResult<MovieListResponse> {
        return safeApiCall.execute { api.searchMovies(query, cursor, limit) }
    }
}
