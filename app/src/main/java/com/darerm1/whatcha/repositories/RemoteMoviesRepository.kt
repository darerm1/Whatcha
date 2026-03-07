package com.darerm1.whatcha.repositories

import android.util.Log
import com.darerm1.whatcha.data.common.NetworkError
import com.darerm1.whatcha.data.common.NetworkResult
import com.darerm1.whatcha.data.interfaces.AllMoviesRepository
import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.data.local.cache.MoviesMemoryCache
import com.darerm1.whatcha.data.remote.datasource.RemoteMoviesDataSource
import com.darerm1.whatcha.data.remote.mapper.MovieMapper
import com.darerm1.whatcha.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class RemoteMoviesRepository(
    private val remoteDataSource: RemoteMoviesDataSource,
    private val cache: MoviesMemoryCache
) : AllMoviesRepository {
    
    companion object {
        private const val TAG = "RemoteMoviesRepository"
        const val PAGE_SIZE = 20
    }
    
     private val currentCursor = AtomicReference<String?>(null)
     @Volatile private var searchQuery: String = ""
     private val isLoading = AtomicBoolean(false)
     @Volatile private var hasNextPage: Boolean = false
    
    override suspend fun searchMovies(query: String, limit: Int): NetworkResult<List<MediaItem>> {
        if (isLoading.get()) {
            Log.d(TAG, "searchMovies: already loading, skipping")
            return NetworkResult.Success(emptyList())
        }
        
        return withContext(Dispatchers.IO) {
            isLoading.set(true)
            try {
                Log.d(TAG, "searchMovies: query='$query', limit=$limit")
                searchQuery = query
                currentCursor.set(null)
                
                when (val result = remoteDataSource.searchMovies(query, null, limit)) {
                    is NetworkResult.Success -> {
                        val movies = result.data.docs.mapNotNull { dto ->
                            val mapped = MovieMapper.mapDtoToDomain(dto)
                            if (mapped is Result.Success) {
                                mapped.data
                            } else {
                                Log.w(TAG, "Failed to map movie ${dto.id}: ${(mapped as Result.Error).message}")
                                null
                            }
                        }
                         movies.forEach { cache.put(it.id, it) }
                         currentCursor.set(result.data.next)
                         hasNextPage = result.data.hasNext
                         Log.d(TAG, "searchMovies: loaded ${movies.size} movies, nextCursor=${currentCursor.get()}, hasNext=$hasNextPage")
                         NetworkResult.Success(movies)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "searchMovies: error=${result.error}")
                        result
                    }
                }
            } finally {
                isLoading.set(false)
            }
        }
    }
    
    override suspend fun loadMore(): NetworkResult<List<MediaItem>> {
        if (isLoading.get()) {
            Log.d(TAG, "loadMore: already loading, skipping")
            return NetworkResult.Success(emptyList())
        }
        
        val cursor = currentCursor.get()
        if (cursor == null || !hasNextPage) {
            Log.d(TAG, "loadMore: no more data (cursor=$cursor, hasNext=$hasNextPage)")
            return NetworkResult.Success(emptyList())
        }
        
        return withContext(Dispatchers.IO) {
            isLoading.set(true)
            try {
                Log.d(TAG, "loadMore: cursor=$cursor")
                
                when (val result = remoteDataSource.searchMovies(searchQuery, cursor, PAGE_SIZE)) {
                    is NetworkResult.Success -> {
                        val movies = result.data.docs.mapNotNull { dto ->
                            val mapped = MovieMapper.mapDtoToDomain(dto)
                            if (mapped is Result.Success) {
                                mapped.data
                            } else {
                                Log.w(TAG, "Failed to map movie ${dto.id}: ${(mapped as Result.Error).message}")
                                null
                            }
                        }
                         movies.forEach { cache.put(it.id, it) }
                         currentCursor.set(result.data.next)
                         hasNextPage = result.data.hasNext
                         Log.d(TAG, "loadMore: loaded ${movies.size} movies, nextCursor=${currentCursor.get()}, hasNext=$hasNextPage")
                         NetworkResult.Success(movies)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "loadMore: error=${result.error}")
                        result
                    }
                }
            } finally {
                isLoading.set(false)
            }
        }
    }
    
    override suspend fun getMovieById(id: Long): NetworkResult<MediaItem> {
        cache.get(id)?.let { return NetworkResult.Success(it) }
        
        return withContext(Dispatchers.IO) {
            when (val result = remoteDataSource.getMovieById(id)) {
                is NetworkResult.Success -> {
                    val mapped = MovieMapper.mapDtoToDomain(result.data)
                    val movie = if (mapped is Result.Success) mapped.data else null
                    if (movie != null) {
                        cache.put(id, movie)
                        NetworkResult.Success(movie)
                    } else {
                        NetworkResult.Error(NetworkError.Unknown("Failed to map movie"))
                    }
                }
                is NetworkResult.Error -> result
            }
        }
    }
    
     override fun clearCache() {
         cache.clear()
         currentCursor.set(null)
         searchQuery = ""
         hasNextPage = false
     }
     
     override fun hasMoreData(): Boolean = currentCursor.get() != null && hasNextPage
}
