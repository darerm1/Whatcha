package com.darerm1.whatcha.infrastructure.network

import android.content.Context
import com.darerm1.whatcha.BuildConfig
import com.darerm1.whatcha.data.enums.Genre
import com.darerm1.whatcha.data.local.stats.RequestStatsStorage
import com.darerm1.whatcha.data.remote.ApiConfig
import com.darerm1.whatcha.data.remote.api.PoiskKinoApi
import com.darerm1.whatcha.data.remote.interceptor.AuthInterceptor
import com.darerm1.whatcha.data.remote.interceptor.RequestCounterInterceptor
import com.darerm1.whatcha.data.serialization.adapters.GenreTypeAdapter
import com.darerm1.whatcha.data.serialization.adapters.LocalDateAdapter
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.util.concurrent.TimeUnit

object NetworkClient {
    fun createApi(context: Context, apiKey: String, applicationScope: CoroutineScope): PoiskKinoApi {
        val statsStorage = RequestStatsStorage(context)
        
        val cache = Cache(context.cacheDir, ApiConfig.CACHE_SIZE_MB * 1024 * 1024)
        
        val okHttpClient = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(AuthInterceptor(apiKey))
            .addInterceptor(RequestCounterInterceptor(statsStorage, applicationScope))
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        }
                    )
                }
            }
            .connectTimeout(ApiConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(ApiConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
        
        val gson = GsonBuilder()
            .registerTypeAdapter(Genre::class.java, GenreTypeAdapter())
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .create()
        
        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(PoiskKinoApi::class.java)
    }
}
