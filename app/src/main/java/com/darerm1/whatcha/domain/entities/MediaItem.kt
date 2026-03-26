package com.darerm1.whatcha.domain.entities

import com.darerm1.whatcha.domain.entities.enums.Genre
import com.darerm1.whatcha.domain.entities.enums.Status
import java.time.LocalDate

interface MediaItem {
    val id: Long

    val name: String

    val year: Int

    val genre: Genre

    var status: Status

    var personalRating: Float?

    val kpRating: Double?

    val ratings: MovieRatings?

    var date: LocalDate?

    val posterUrl: String?

    fun contentEquals(other: MediaItem): Boolean
}