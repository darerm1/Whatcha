package com.darerm1.whatcha.repositories

import com.darerm1.whatcha.data.interfaces.AllMoviesRepository
import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.data.enums.Genre
import com.darerm1.whatcha.data.models.Movie

class AllMoviesRepositoryImpl: AllMoviesRepository {
    private val allMovies = listOf<MediaItem>(
        Movie(
            id = 1,
            name = "Interstellar",
            year = 2014, description = "time, space, life",
            trailerUrl = "...",
            genre = Genre.DRAMA,
            duration = 169
        ),
        Movie(
            id = 2,
            name = "Oldboy",
            year = 2003,
            description = "vengeance, fault, strength",
            trailerUrl = "...",
            genre = Genre.THRILLER,
            duration = 120
        ),
        Movie(
            id = 3,
            name = "The Blair Witch Project",
            year = 1999,
            description = "forest, students, mistery",
            trailerUrl = "...",
            genre = Genre.HORROR,
            duration = 81
        ),
        Movie(
            id = 4,
            name = "The Lego Batman Monie",
            year = 2017,
            description = "cool, hero, loneliness",
            trailerUrl = "...",
            genre = Genre.COMEDY,
            duration = 104
        )
    )

    override fun searchMovies(query: String): List<MediaItem> {
        return allMovies.filter { a -> a.name.contains(query, ignoreCase = true) }
    }

    override fun getMovieById(id: Long): MediaItem? {
        return allMovies.find { a -> a.id == id }
    }
}