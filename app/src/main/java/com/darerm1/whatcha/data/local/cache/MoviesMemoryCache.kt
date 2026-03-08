package com.darerm1.whatcha.data.local.cache

import com.darerm1.whatcha.data.interfaces.MediaItem

class MoviesMemoryCache(private val maxSize: Int = 100) {
    private val cache = object : LinkedHashMap<Long, MediaItem>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, MediaItem>?): Boolean {
            return size > maxSize
        }
    }
    
    @Synchronized
    fun put(id: Long, movie: MediaItem) {
        cache[id] = movie
    }
    
    @Synchronized
    fun get(id: Long): MediaItem? = cache[id]
    
    @Synchronized
    fun clear() {
        cache.clear()
    }
    
    @Synchronized
    fun size(): Int = cache.size
    
    @Synchronized
    fun getAll(): List<MediaItem> = cache.values.toList()
}
