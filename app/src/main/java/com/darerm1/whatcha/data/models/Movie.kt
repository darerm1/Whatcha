package com.darerm1.whatcha.data.models

import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.data.enums.Genre
import com.darerm1.whatcha.data.enums.Status
import java.time.LocalDate

data class Movie(
    override val id: Long,

    override val name: String,

    override val year: Int,

    val description: String,

    override val genre: Genre,

    val duration: Int,

    val trailerUrl: String,

    override var personalRating: Int? = null,

    override var status: Status = Status.NOT_SET,

    override val date: LocalDate? = null,

    override val posterUrl: String? = null
): MediaItem