package com.darerm1.whatcha.data.remote.api

import com.darerm1.whatcha.data.remote.dto.MovieDto
import com.darerm1.whatcha.data.remote.dto.MovieListResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PoiskKinoApi {
    @GET("/v1.4/movie/{id}")
    suspend fun getMovieById(@Path("id") id: Long): MovieDto
    
    @GET("/v1.5/movie")
    suspend fun searchMovies(
        @Query("query") query: String?,
        @Query("next") next: String?,
        @Query("limit") limit: Int = 20
    ): MovieListResponse
}
