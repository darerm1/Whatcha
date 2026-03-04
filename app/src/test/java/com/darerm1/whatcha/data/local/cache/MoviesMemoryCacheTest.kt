package com.darerm1.whatcha.data.local.cache

import com.darerm1.whatcha.data.enums.Genre
import com.darerm1.whatcha.data.enums.Status
import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.data.models.Movie
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
import org.junit.Test

class MoviesMemoryCacheTest {
    
    @Test
    fun `put and get should work correctly`() {
        val cache = MoviesMemoryCache(maxSize = 2)
        val movie = mockk<MediaItem>(relaxed = true)
        every { movie.id } returns 1
        
        cache.put(1, movie)
        
        assertEquals(movie, cache.get(1))
    }
    
    @Test
    fun `get should return null for non-existent id`() {
        val cache = MoviesMemoryCache()
        
        assertNull(cache.get(999))
    }
    
    @Test
    fun `cache should evict eldest entries when exceeding maxSize`() {
        val cache = MoviesMemoryCache(maxSize = 2)
        val movie1 = mockk<MediaItem>(relaxed = true) { every { id } returns 1 }
        val movie2 = mockk<MediaItem>(relaxed = true) { every { id } returns 2 }
        val movie3 = mockk<MediaItem>(relaxed = true) { every { id } returns 3 }
        
        cache.put(1, movie1)
        cache.put(2, movie2)
        cache.put(3, movie3)
        
        assertNull(cache.get(1)) // movie1 should be evicted
        assertNotNull(cache.get(2))
        assertNotNull(cache.get(3))
    }
    
    @Test
    fun `clear should remove all entries`() {
        val cache = MoviesMemoryCache()
        val movie1 = mockk<MediaItem>(relaxed = true) { every { id } returns 1 }
        val movie2 = mockk<MediaItem>(relaxed = true) { every { id } returns 2 }
        
        cache.put(1, movie1)
        cache.put(2, movie2)
        
        assertEquals(2, cache.size())
        
        cache.clear()
        
        assertEquals(0, cache.size())
        assertNull(cache.get(1))
        assertNull(cache.get(2))
    }
    
    @Test
    fun `getAll should return all cached items`() {
        val cache = MoviesMemoryCache()
        val movie1 = mockk<MediaItem>(relaxed = true) { every { id } returns 1 }
        val movie2 = mockk<MediaItem>(relaxed = true) { every { id } returns 2 }
        
        cache.put(1, movie1)
        cache.put(2, movie2)
        
        val all = cache.getAll()
        
        assertEquals(2, all.size)
        assertTrue(all.contains(movie1))
        assertTrue(all.contains(movie2))
    }
}
