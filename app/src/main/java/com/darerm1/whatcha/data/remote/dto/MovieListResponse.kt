package com.darerm1.whatcha.data.remote.dto

data class MovieListResponse(
    val docs: List<MovieDto>,
    val limit: Int,
    val next: String?,
    val prev: String?,
    val hasNext: Boolean,
    val hasPrev: Boolean
)
