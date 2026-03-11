package com.darerm1.whatcha.data.remote.mapper

import android.util.Log
import com.darerm1.whatcha.data.enums.Genre
import com.darerm1.whatcha.data.enums.Status
import com.darerm1.whatcha.data.models.Movie
import com.darerm1.whatcha.data.models.MovieRatings
import com.darerm1.whatcha.data.remote.dto.GenreDto
import com.darerm1.whatcha.data.remote.dto.MovieDto
import com.darerm1.whatcha.utils.Result

object MovieMapper {
    private const val TAG = "MovieMapper"

    fun mapDtoToDomain(dto: MovieDto): Result<Movie> {
        return try {
            val genre = findSupportedGenre(dto.genres)

            val movieName = dto.name
                ?: dto.names?.find { it.language == "RU" && it.type?.contains("Russian") == true }?.name
                ?: dto.alternativeName
                ?: dto.names?.firstOrNull()?.name
                ?: "Unknown"

            val posterUrl = dto.poster?.url ?: dto.poster?.previewUrl
            val normalizedRatings = dto.rating?.toDomainRatings()?.takeIf { it.hasAnyValue() }

            Result.Success(
                Movie(
                    id = dto.id,
                    name = movieName,
                    year = dto.year ?: 0,
                    description = dto.description ?: "",
                    genre = genre,
                    posterUrl = posterUrl,
                    duration = dto.movieLength ?: 0,
                    personalRating = null,
                    kpRating = normalizedRatings?.kp,
                    ratings = normalizedRatings,
                    status = Status.NOT_SET,
                    date = null
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to map movie ${dto.id}: ${e.message}", e)
            Result.Error("Failed to map movie: ${e.message}", e)
        }
    }

    private fun findSupportedGenre(genres: List<GenreDto>?): Genre {
        if (genres.isNullOrEmpty()) return Genre.DRAMA

        for (genreDto in genres) {
            val genreName = genreDto.name ?: continue
            Genre.values().find { it.russianName == genreName }?.let { return it }
        }

        return Genre.DRAMA
    }

    private fun com.darerm1.whatcha.data.remote.dto.RatingDto.toDomainRatings(): MovieRatings {
        return MovieRatings(
            kp = kp.normalizeRating(),
            imdb = imdb.normalizeRating(),
            tmdb = tmdb.normalizeRating(),
            filmCritics = filmCritics.normalizeRating(),
            russianFilmCritics = russianFilmCritics.normalizeRating(),
            await = await.normalizeRating()
        )
    }

    private fun Double?.normalizeRating(): Double? = this?.takeIf { it > 0.0 }

    private fun MovieRatings.hasAnyValue(): Boolean {
        return kp != null || imdb != null || tmdb != null || filmCritics != null || russianFilmCritics != null || await != null
    }
}
