package com.darerm1.whatcha.data.models

import com.darerm1.whatcha.data.enums.Genre
import com.darerm1.whatcha.data.enums.Status
import com.darerm1.whatcha.data.interfaces.MediaItem
import java.time.LocalDate

data class Movie(
    override val id: Long,
    override val name: String,
    override val year: Int,
    val description: String,
    override val genre: Genre,
    val duration: Int,
    override var personalRating: Float? = null,
    override val kpRating: Double? = null,
    override val ratings: MovieRatings? = kpRating?.let { MovieRatings(kp = it) },
    override var status: Status = Status.NOT_SET,
    override var date: LocalDate? = null,
    override val posterUrl: String? = null
) : MediaItem {
    override fun contentEquals(other: MediaItem): Boolean {
        if (other !is Movie) return false

        return this.id == other.id &&
                this.name == other.name &&
                this.year == other.year &&
                this.posterUrl == other.posterUrl &&
                this.description == other.description &&
                this.genre == other.genre &&
                this.duration == other.duration &&
                this.personalRating == other.personalRating &&
                this.kpRating == other.kpRating &&
                this.ratings == other.ratings &&
                this.status == other.status &&
                this.date == other.date
    }
}
