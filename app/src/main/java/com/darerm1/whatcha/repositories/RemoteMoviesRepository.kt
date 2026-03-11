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

    private val currentNext = AtomicReference<String?>(null)
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
                Log.d(TAG, "=== searchMovies START ===")
                Log.d(TAG, "query='$query', limit=$limit")

                searchQuery = query
                currentNext.set(null)

                when (val result = remoteDataSource.searchMovies(query, null, limit)) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "API returned ${result.data.docs.size} movies")
                        Log.d(TAG, "API response: next=${result.data.next}, hasNext=${result.data.hasNext}")

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
                        currentNext.set(result.data.next)
                        hasNextPage = result.data.hasNext

                        Log.d(TAG, "Loaded ${movies.size} movies, nextToken=${currentNext.get()}, hasNext=$hasNextPage")
                        Log.d(TAG, "=== searchMovies END (Success) ===")

                        NetworkResult.Success(movies)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "searchMovies: error=${result.error}")
                        Log.d(TAG, "=== searchMovies END (Error) ===")
                        result
                    }
                }
            } finally {
                isLoading.set(false)
            }
        }
    }

    override suspend fun loadMore(): NetworkResult<List<MediaItem>> {
        Log.d(TAG, "=== loadMore START ===")
        Log.d(TAG, "isLoading=${isLoading.get()}, next=${currentNext.get()}, hasNext=$hasNextPage")

        if (isLoading.get()) {
            Log.d(TAG, "loadMore: already loading, skipping")
            return NetworkResult.Success(emptyList())
        }

        val nextToken = currentNext.get()
        if (nextToken == null || !hasNextPage) {
            Log.d(TAG, "loadMore: no more data (next=$nextToken, hasNext=$hasNextPage)")
            return NetworkResult.Success(emptyList())
        }

        return withContext(Dispatchers.IO) {
            isLoading.set(true)
            try {
                Log.d(TAG, "loadMore: calling API with next=$nextToken, query='$searchQuery'")

                when (val result = remoteDataSource.searchMovies(searchQuery, nextToken, PAGE_SIZE)) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "API returned ${result.data.docs.size} movies")
                        Log.d(TAG, "API response: next=${result.data.next}, hasNext=${result.data.hasNext}")

                        val movies = result.data.docs.mapNotNull { dto ->
                            val mapped = MovieMapper.mapDtoToDomain(dto)
                            if (mapped is Result.Success) {
                                mapped.data
                            } else {
                                Log.w(TAG, "Failed to map movie ${dto.id}: ${(mapped as Result.Error).message}")
                                null
                            }
                        }

                        Log.d(TAG, "Successfully mapped ${movies.size} movies")

                        movies.forEach { cache.put(it.id, it) }
                        currentNext.set(result.data.next)
                        hasNextPage = result.data.hasNext

                        Log.d(TAG, "Updated: next=${currentNext.get()}, hasNext=$hasNextPage")
                        Log.d(TAG, "=== loadMore END (Success) ===")

                        NetworkResult.Success(movies)
                    }
                    is NetworkResult.Error -> {
                        Log.e(TAG, "loadMore: API error=${result.error}")
                        Log.d(TAG, "=== loadMore END (Error) ===")
                        result
                    }
                }
            } finally {
                isLoading.set(false)
            }
        }
    }

    override suspend fun getMovieById(id: Long): NetworkResult<MediaItem> {
        cache.get(id)?.let { cachedMovie ->
            return NetworkResult.Success(cachedMovie)
        }

        return withContext(Dispatchers.IO) {
            when (val result = remoteDataSource.getMovieById(id)) {
                is NetworkResult.Success -> {
                    val mapped = MovieMapper.mapDtoToDomain(result.data)
                    if (mapped is Result.Success) {
                        cache.put(id, mapped.data)
                        NetworkResult.Success(mapped.data)
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
        currentNext.set(null)
        searchQuery = ""
        hasNextPage = false
    }

    override fun hasMoreData(): Boolean {
        val nextToken = currentNext.get()
        val result = nextToken != null && hasNextPage
        Log.d(TAG, "hasMoreData: next=$nextToken, hasNext=$hasNextPage, result=$result")
        return result
    }
}