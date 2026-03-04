package com.darerm1.whatcha.data.remote

import com.darerm1.whatcha.data.common.NetworkError
import com.darerm1.whatcha.data.common.NetworkResult
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class SafeApiCall {
    suspend fun <T> execute(apiCall: suspend () -> T): NetworkResult<T> {
        return try {
            NetworkResult.Success(apiCall())
        } catch (e: UnknownHostException) {
            NetworkResult.Error(NetworkError.NoInternet)
        } catch (e: SocketTimeoutException) {
            NetworkResult.Error(NetworkError.Timeout)
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> NetworkResult.Error(NetworkError.Unauthorized)
                429 -> NetworkResult.Error(NetworkError.RateLimitExceeded)
                in 500..599 -> NetworkResult.Error(NetworkError.ServerError(e.code()))
                else -> NetworkResult.Error(NetworkError.Unknown(e.message()))
            }
        } catch (e: Exception) {
            NetworkResult.Error(NetworkError.Unknown(e.message))
        }
    }
}
