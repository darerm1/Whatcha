package com.darerm1.whatcha.infrastructure

import com.darerm1.whatcha.data.interfaces.AllMoviesRepository
import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.repositories.AllMoviesRepositoryImpl

class AllMoviesService private constructor(private val allMoviesRepo: AllMoviesRepository) {

    companion object {
        val instance: AllMoviesService by lazy { AllMoviesService(AllMoviesRepositoryImpl.instance) }
    }

    fun searchMovies(query: String): List<MediaItem> { 
        return allMoviesRepo.searchMovies(query)
    }

    fun searchMovies(query: String, page: Int, pageSize: Int): List<MediaItem> {
        return allMoviesRepo.searchMovies(query, page, pageSize)
    }

    fun getMovieById(id: Long): MediaItem? { 
        return allMoviesRepo.getMovieById(id)
    }
}