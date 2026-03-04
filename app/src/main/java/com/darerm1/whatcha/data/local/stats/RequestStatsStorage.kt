package com.darerm1.whatcha.data.local.stats

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class RequestStatsStorage(private val context: Context) {
    private val file = File(context.filesDir, "request_stats.json")
    private val gson = Gson()
    private val lock = Any()
    
    suspend fun incrementRequestCount(movieId: Long) = withContext(Dispatchers.IO) {
        synchronized(lock) {
            val stats = getAllStatsInternal()
            stats[movieId] = (stats[movieId] ?: 0) + 1
            file.writeText(gson.toJson(stats))
        }
    }
    
    suspend fun getRequestCount(movieId: Long): Int = withContext(Dispatchers.IO) {
        synchronized(lock) {
            getAllStatsInternal()[movieId] ?: 0
        }
    }
    
    suspend fun getAllStats(): Map<Long, Int> = withContext(Dispatchers.IO) {
        synchronized(lock) {
            getAllStatsInternal().toMap()
        }
    }
    
    private fun getAllStatsInternal(): MutableMap<Long, Int> {
        return if (file.exists()) {
            try {
                gson.fromJson(file.readText(), object : TypeToken<MutableMap<Long, Int>>() {}.type)
            } catch (e: Exception) {
                mutableMapOf()
            }
        } else {
            mutableMapOf()
        }
    }
}
