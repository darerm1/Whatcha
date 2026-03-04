package com.darerm1.whatcha.data.remote.dto

@Suppress("ConstructorParameterNaming")
data class VideoTypesDto(
    val trailers: List<VideoDto>?,
    val teasers: List<VideoDto>?
)
