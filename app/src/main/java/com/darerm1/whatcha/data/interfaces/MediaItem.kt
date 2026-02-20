package com.darerm1.whatcha.data.interfaces

import com.darerm1.whatcha.data.enums.Genre
import com.darerm1.whatcha.data.enums.Status
import java.time.LocalDate

interface MediaItem {
    val id: Long

    val name: String

    val year: Int

    val genre: Genre

    var status: Status

    var personalRating: Int?

    val date: LocalDate?
}
