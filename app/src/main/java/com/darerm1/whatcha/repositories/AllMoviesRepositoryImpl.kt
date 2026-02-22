package com.darerm1.whatcha.repositories

import com.darerm1.whatcha.data.interfaces.AllMoviesRepository
import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.data.enums.Genre
import com.darerm1.whatcha.data.models.Movie

class AllMoviesRepositoryImpl: AllMoviesRepository {

    companion object {
        val instance: AllMoviesRepositoryImpl by lazy { AllMoviesRepositoryImpl() }
    }
    private val allMovies = listOf<MediaItem>(
        Movie(
            id = 1,
            name = "Interstellar",
            year = 2014,
            description = "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
            trailerUrl = "https://youtube.com/watch?v=zSWdZVtXT7E",
            genre = Genre.DRAMA,
            duration = 169,
            posterUrl = "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg"
        ),
        Movie(
            id = 2,
            name = "Oldboy",
            year = 2003,
            description = "After being kidnapped and imprisoned for fifteen years, Oh Dae-Su is released, only to find that he must find his captor in five days.",
            trailerUrl = "https://youtube.com/watch?v=2HkjrJ6IK5E",
            genre = Genre.THRILLER,
            duration = 120,
            posterUrl = "https://image.tmdb.org/t/p/w500/b9SKkRp0hbvMhs2IXvBwKP9eRQA.jpg"
        ),
        Movie(
            id = 3,
            name = "The Blair Witch Project",
            year = 1999,
            description = "Three film students vanish after traveling into a Maryland forest to film a documentary on the local Blair Witch legend, leaving only their footage behind.",
            trailerUrl = "https://youtube.com/watch?v=a_Hw4bdsjTo",
            genre = Genre.HORROR,
            duration = 81,
            posterUrl = "https://image.tmdb.org/t/p/w500/qYSe5YA7bZ4bRhGqrGkJK1bOCyV.jpg"
        ),
        Movie(
            id = 4,
            name = "The Lego Batman Movie",
            year = 2017,
            description = "A cooler-than-ever Bruce Wayne must deal with the usual suspects as they plan to rule Gotham City.",
            trailerUrl = "https://youtube.com/watch?v=fG_M6CN1C8w",
            genre = Genre.COMEDY,
            duration = 104,
            posterUrl = "https://image.tmdb.org/t/p/w500/xRM8O5U3T3RqLDBLJi7UwqPe7Li.jpg"
        ),
        Movie(
            id = 5,
            name = "Inception",
            year = 2010,
            description = "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.",
            trailerUrl = "https://youtube.com/watch?v=YoHD9XEInc0",
            genre = Genre.THRILLER,
            duration = 148,
            posterUrl = "https://image.tmdb.org/t/p/w500/ljsZTbVsrQSqZgWeep9B9iMrXfs.jpg"
        ),
        Movie(
            id = 6,
            name = "The Matrix",
            year = 1999,
            description = "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.",
            trailerUrl = "https://youtube.com/watch?v=vKQi3bBA1y8",
            genre = Genre.FANTASY,
            duration = 136,
            posterUrl = "https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg"
        ),
        Movie(
            id = 7,
            name = "Pulp Fiction",
            year = 1994,
            description = "The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.",
            trailerUrl = "https://youtube.com/watch?v=s7EdQ4FqbhY",
            genre = Genre.DRAMA,
            duration = 154,
            posterUrl = "https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg"
        ),
        Movie(
            id = 8,
            name = "The Shawshank Redemption",
            year = 1994,
            description = "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.",
            trailerUrl = "https://youtube.com/watch?v=6hB3S9bIaco",
            genre = Genre.DRAMA,
            duration = 142,
            posterUrl = "https://image.tmdb.org/t/p/w500/q6y0Go1tsGEsmtFryDOJo3dEmqu.jpg"
        ),
        Movie(
            id = 9,
            name = "Fight Club",
            year = 1999,
            description = "An insomniac office worker and a devil-may-care soapmaker form an underground fight club that evolves into something much more.",
            trailerUrl = "https://youtube.com/watch?v=SUXWAEX2jlg",
            genre = Genre.THRILLER,
            duration = 139,
            posterUrl = "https://image.tmdb.org/t/p/w500/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg"
        ),
        Movie(
            id = 10,
            name = "Forrest Gump",
            year = 1994,
            description = "The presidencies of Kennedy and Johnson, the Vietnam War, the Watergate scandal and other historical events unfold from the perspective of an Alabama man with an IQ of 75.",
            trailerUrl = "https://youtube.com/watch?v=bLvqoHBptjg",
            genre = Genre.DRAMA,
            duration = 142,
            posterUrl = "https://image.tmdb.org/t/p/w500/arw2vcBveWOVZr6pxd9XTd1TdQa.jpg"
        ),
        Movie(
            id = 11,
            name = "The Godfather",
            year = 1972,
            description = "The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son.",
            trailerUrl = "https://youtube.com/watch?v=sY1S34973zA",
            genre = Genre.DRAMA,
            duration = 175,
            posterUrl = "https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja1.jpg"
        ),
        Movie(
            id = 12,
            name = "The Dark Knight",
            year = 2008,
            description = "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.",
            trailerUrl = "https://youtube.com/watch?v=EXeTwQWrcwY",
            genre = Genre.THRILLER,
            duration = 152,
            posterUrl = "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg"
        ),
        Movie(
            id = 13,
            name = "Schindler's List",
            year = 1993,
            description = "In German-occupied Poland during World War II, industrialist Oskar Schindler gradually becomes concerned for his Jewish workforce after witnessing their persecution by the Nazis.",
            trailerUrl = "https://youtube.com/watch?v=gG22XNhtbaY",
            genre = Genre.DRAMA,
            duration = 195,
            posterUrl = "https://image.tmdb.org/t/p/w500/sF1U4EUQS8YHUYjNl3pMGNIQyr0.jpg"
        ),
        Movie(
            id = 14,
            name = "The Silence of the Lambs",
            year = 1991,
            description = "A young F.B.I. cadet must receive the help of an incarcerated and manipulative cannibal killer to help catch another serial killer.",
            trailerUrl = "https://youtube.com/watch?v=W6Mm8Sbe__I",
            genre = Genre.HORROR,
            duration = 118,
            posterUrl = "https://image.tmdb.org/t/p/w500/rplLJkzM5PB4pgO5Tqk4X9D1WlL.jpg"
        ),
        Movie(
            id = 15,
            name = "Planet Earth",
            year = 2006,
            description = "Emmy Award-winning, 11 episodes, five years in the making, the most expensive nature documentary series ever commissioned by the BBC, and the first to be filmed in high definition.",
            trailerUrl = "https://youtube.com/watch?v=RvP3FWkLAOA",
            genre = Genre.DOCUMENTARY,
            duration = 550,
            posterUrl = "https://image.tmdb.org/t/p/w500/2b7Z8mU0z7NtTaPHQ5QdL1wZPFi.jpg"
        )
    )

    override fun searchMovies(query: String): List<MediaItem> {
        return allMovies.filter { a -> a.name.contains(query, ignoreCase = true) }
    }

    override fun searchMovies(query: String, page: Int, pageSize: Int): List<MediaItem> {
        val filtered = allMovies.filter { a -> a.name.contains(query, ignoreCase = true) }
        val startIndex = (page - 1) * pageSize
        if (startIndex >= filtered.size) return emptyList()
        val endIndex = minOf(startIndex + pageSize, filtered.size)
        return filtered.subList(startIndex, endIndex)
    }

    override fun getMovieById(id: Long): MediaItem? {
        return allMovies.find { a -> a.id == id }
    }
}
