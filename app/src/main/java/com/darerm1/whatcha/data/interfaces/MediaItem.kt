package com.darerm1.whatcha.data.interfaces

import com.darerm1.whatcha.data.enums.Genre
import com.darerm1.whatcha.data.enums.Status
import com.darerm1.whatcha.data.models.MovieRatings
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
