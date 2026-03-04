package com.darerm1.whatcha.data.remote.dto

@Suppress("ConstructorParameterNaming")
data class VotesDto(
    val kp: Int?,
    val imdb: Int?,
    val filmCritics: Int?,
    val russianFilmCritics: Int?,
    val await: Int?
)
