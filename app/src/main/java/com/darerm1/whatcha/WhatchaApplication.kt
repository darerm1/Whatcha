package com.darerm1.whatcha

import android.app.Application
import com.darerm1.whatcha.data.local.cache.MoviesMemoryCache
import com.darerm1.whatcha.data.remote.datasource.RemoteMoviesDataSource
import com.darerm1.whatcha.infrastructure.network.NetworkClient
import com.darerm1.whatcha.repositories.RemoteMoviesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class WhatchaApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val cache = MoviesMemoryCache()
    
    private val api by lazy {
        NetworkClient.createApi(this, BuildConfig.API_KEY, applicationScope)
    }
    
    val repository by lazy {
        RemoteMoviesRepository(
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
