package com.darerm1.whatcha.data.interfaces

import com.darerm1.whatcha.data.enums.Status

interface MediaItem {
    val id: Long

    val name: String

    val year: Int

    var status: Status

    var personalRating: Int?
}