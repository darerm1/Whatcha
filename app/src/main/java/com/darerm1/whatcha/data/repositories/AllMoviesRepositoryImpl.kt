package com.darerm1.whatcha.data.repositories

import android.util.Log
import com.darerm1.whatcha.data.common.NetworkError
import com.darerm1.whatcha.data.common.NetworkResult
import com.darerm1.whatcha.domain.entities.MediaItem
import com.darerm1.whatcha.data.local.cache.MoviesMemoryCache
import com.darerm1.whatcha.data.remote.datasource.RemoteMoviesDataSource
import com.darerm1.whatcha.data.remote.mappers.MovieMapper
import com.darerm1.whatcha.domain.repositories.AllMoviesRepository
import com.darerm1.whatcha.domain.common.Result
import com.darerm1.whatcha.domain.common.DomainError
import com.darerm1.whatcha.domain.common.SearchConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class AllMoviesRepositoryImpl(
    private val remoteDataSource: RemoteMoviesDataSource,
    private val cache: MoviesMemoryCache
) : AllMoviesRepository {

    companion object {
        private const val TAG = "RemoteMoviesRepository"
        const val PAGE_SIZE = SearchConfig.DEFAULT_PAGE_SIZE
    }

    private val currentNext = AtomicReference<String?>(null)
    @Volatile private var searchQuery: String = ""
    private val isLoading = AtomicBoolean(false)
    @Volatile private var hasNextPage: Boolean = false

    private fun NetworkError.toDomainError(): DomainError = when (this) {
        is NetworkError.NoInternet -> DomainError.NoInternet
        is NetworkError.Timeout -> DomainError.Timeout
        is NetworkError.Unauthorized -> DomainError.Unauthorized
        is NetworkError.RateLimitExceeded -> DomainError.RateLimitExceeded
        is NetworkError.ServerError -> DomainError.ServerError(code)
        is NetworkError.Unknown -> DomainError.Unknown(message)
    }

    override suspend fun searchMovies(query: String, limit: Int): Result<List<MediaItem>> {
        if (isLoading.get()) {
            Log.d(TAG, "searchMovies: already loading, skipping")
            return Result.Success(emptyList())
        }

        return withContext(Dispatchers.IO) {
            isLoading.set(true)
            try {
                Log.d(TAG, "SearchMovies start")
                Log.d(TAG, "query='$query', limit=$limit")

                searchQuery = query
                currentNext.set(null)

                when (val result = remoteDataSource.searchMovies(query, null, limit)) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "API returned ${result.data.docs.size} movies")
                        Log.d(
                            TAG,
                            "API response: next=${result.data.next}, hasNext=${result.data.hasNext}"
                        )

                        val movies = result.data.docs.mapNotNull { dto ->
                            val mapped = MovieMapper.mapDtoToDomain(dto)
                            if (mapped is Result.Success) {
                                mapped.data
                            } else {
                                Log.w(
                                    TAG,
                                    "Failed to map movie ${dto.id}: ${(mapped as Result.Error).error}"
                                )
                                null
                            }
                        }

                        movies.forEach { cache.put(it.id, it) }
                        currentNext.set(result.data.next)
                        hasNextPage = result.data.hasNext

                        Log.d(
                            TAG,
                            "Loaded ${movies.size} movies, nextToken=${currentNext.get()}, hasNext=$hasNextPage"
                        )
                        Log.d(TAG, "SearchMovies successfully ended")

                        Result.Success(movies)
                    }

                    is NetworkResult.Error -> {
                        Log.e(TAG, "searchMovies ended: error=${result.error}")
                        Result.Error(result.error.toDomainError())
                    }
                }
            } finally {
                isLoading.set(false)
            }
        }
    }

    override suspend fun loadMore(): Result<List<MediaItem>> {
        Log.d(TAG, "LoadMore start")
        Log.d(TAG, "isLoading=${isLoading.get()}, next=${currentNext.get()}, hasNext=$hasNextPage")

        if (isLoading.get()) {
            Log.d(TAG, "loadMore: already loading, skipping")
            return Result.Success(emptyList())
        }

        val nextToken = currentNext.get()
        if (nextToken == null || !hasNextPage) {
            Log.d(TAG, "loadMore: no more data (next=$nextToken, hasNext=$hasNextPage)")
            return Result.Success(emptyList())
        }

        return withContext(Dispatchers.IO) {
            isLoading.set(true)
            try {
                Log.d(TAG, "loadMore: calling API with next=$nextToken, query='$searchQuery'")

                when (val result = remoteDataSource.searchMovies(searchQuery, nextToken, PAGE_SIZE)) {
                    is NetworkResult.Success -> {
                        Log.d(TAG, "API returned ${result.data.docs.size} movies")
                        Log.d(TAG,"API response: next=${result.data.next}, hasNext=${result.data.hasNext}")

                        val movies = result.data.docs.mapNotNull { dto ->
                            val mapped = MovieMapper.mapDtoToDomain(dto)
                            if (mapped is Result.Success) {
                                mapped.data
                            } else {
                                Log.w(TAG, "Failed to map movie ${dto.id}: ${(mapped as Result.Error).error}")
                                null
                            }
                        }

                        Log.d(TAG, "Successfully mapped ${movies.size} movies")

                        movies.forEach { cache.put(it.id, it) }
                        currentNext.set(result.data.next)
                        hasNextPage = result.data.hasNext

                        Log.d(TAG, "Updated: next=${currentNext.get()}, hasNext=$hasNextPage")
                        Log.d(TAG, "LoadMore successfully end")

                        Result.Success(movies)
                    }

                    is NetworkResult.Error -> {
                        Log.e(TAG, "loadMore ended with error: API error=${result.error}")
                        Result.Error(result.error.toDomainError())
                    }
                }
            } finally {
                isLoading.set(false)
            }
        }
    }

    override suspend fun getMovieById(id: Long): Result<MediaItem> {
        cache.get(id)?.let { cachedMovie ->
            return Result.Success(cachedMovie)
        }

        return withContext(Dispatchers.IO) {
            when (val result = remoteDataSource.getMovieById(id)) {
                is NetworkResult.Success -> {
                    val mapped = MovieMapper.mapDtoToDomain(result.data)
                    if (mapped is Result.Success) {
                        cache.put(id, mapped.data)
                        Result.Success(mapped.data)
                    } else {
                        mapped
                    }
                }

                is NetworkResult.Error -> Result.Error(result.error.toDomainError())
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