package com.darerm1.whatcha.data.remote.dto

data class MovieListResponse(
    val docs: List<MovieDto>,
    val next: String?,
    val prev: String?
)
