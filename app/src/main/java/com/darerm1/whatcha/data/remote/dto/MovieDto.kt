package com.darerm1.whatcha.data.remote.dto

@Suppress("ConstructorParameterNaming")
data class MovieDto(
    val id: Long,
    val name: String?,
    val year: Int?,
    val description: String?,
    val genres: List<GenreDto>?,
    val poster: PosterDto?,
    val movieLength: Int?,
    val rating: RatingDto?,
    val videos: VideoTypesDto?
)
