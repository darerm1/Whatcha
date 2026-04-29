package com.darerm1.whatcha.data.sdui.network

import android.util.Log
import com.darerm1.whatcha.data.common.NetworkError
import com.darerm1.whatcha.data.common.NetworkResult
import com.darerm1.whatcha.data.sdui.models.SDUIScreen
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class SDUILoader {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun loadScreen(url: String): NetworkResult<SDUIScreen> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Loading SDUI screen from: $url")
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).execute()

            Log.d(TAG, "Response code: ${response.code}")

            if (!response.isSuccessful) {
                Log.e(TAG, "Server error: ${response.code}")
                return@withContext NetworkResult.Error(NetworkError.ServerError(response.code))
            }

            val body = response.body?.string()
            if (body == null) {
                Log.e(TAG, "Empty response body")
                return@withContext NetworkResult.Error(NetworkError.Unknown("Empty response body"))
            }

            Log.d(TAG, "Response body (first 200 chars): ${body.take(200)}")

            val screen = gson.fromJson(body, SDUIScreen::class.java)
            Log.d(TAG, "Parsed ${screen.components.size} components")
            NetworkResult.Success(screen)
        } catch (e: UnknownHostException) {
            Log.e(TAG, "No internet", e)
            NetworkResult.Error(NetworkError.NoInternet)
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout", e)
            NetworkResult.Error(NetworkError.Timeout)
        } catch (e: IOException) {
            Log.e(TAG, "IO error", e)
            NetworkResult.Error(NetworkError.Unknown(e.message))
        } catch (e: Exception) {
            Log.e(TAG, "Unknown error", e)
            NetworkResult.Error(NetworkError.Unknown(e.message))
        }
    }

    companion object {
        private const val TAG = "SDUILoader"
    }
}
