package com.darerm1.whatcha.data.remote.dto

@Suppress("ConstructorParameterNaming")
data class RatingDto(
    val kp: Double?,
    val imdb: Double?,
    val filmCritics: Double?,
    val russianFilmCritics: Double?,
    val await: Double?
)
