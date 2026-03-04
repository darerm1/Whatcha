package com.darerm1.whatcha.data.remote.dto

@Suppress("ConstructorParameterNaming")
data class VideoDto(
    val url: String?,
    val name: String?,
    val site: String?,
    val type: String?,
    val size: Int?
)
