package com.darerm1.whatcha.data.remote.interceptor

import com.darerm1.whatcha.data.local.stats.RequestStatsStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response

class RequestCounterInterceptor(
    private val statsStorage: RequestStatsStorage,
    private val applicationScope: CoroutineScope
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        
        val movieId = request.url.pathSegments.lastOrNull()?.toLongOrNull()
        if (movieId != null && response.isSuccessful) {
            applicationScope.launch {
                statsStorage.incrementRequestCount(movieId)
            }
        }
        
        return response
    }
}
