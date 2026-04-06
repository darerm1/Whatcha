package com.darerm1.whatcha

import android.app.Application
import com.darerm1.whatcha.data.local.cache.MoviesMemoryCache
import com.darerm1.whatcha.data.remote.datasource.RemoteMoviesDataSource
import com.darerm1.whatcha.data.network.NetworkClient
import com.darerm1.whatcha.data.repositories.AllMoviesRepositoryImpl
import com.darerm1.whatcha.data.repositories.MovieListRepositoryImpl
import com.darerm1.whatcha.domain.repositories.AllMoviesRepository
import com.darerm1.whatcha.domain.usecases.ManageMovieListUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class WhatchaApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val cache = MoviesMemoryCache()
    
    private val api by lazy {
        NetworkClient.createApi(this, BuildConfig.API_KEY, applicationScope)
    }

    val manageMovieListUseCase: ManageMovieListUseCase by lazy {
        ManageMovieListUseCase(MovieListRepositoryImpl.instance)
    }

    val repository: AllMoviesRepository by lazy {
        AllMoviesRepositoryImpl(
            remoteDataSource = RemoteMoviesDataSource(api),
            cache = cache
        )
    }
    
    companion object {
        lateinit var instance: WhatchaApplication
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
