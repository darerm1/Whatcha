package com.darerm1.whatcha.infrastructure

import com.darerm1.whatcha.data.interfaces.AllMoviesRepository
import com.darerm1.whatcha.data.interfaces.MediaItem

class AllMoviesService(val allMoviesRepo: AllMoviesRepository) {

    fun searchMovies(query: String): List<MediaItem> { 
        return allMoviesRepo.searchMovies(query)
    }

    fun getMovieById(id: Long): MediaItem? { 
        return allMoviesRepo.getMovieById(id)
    }
}