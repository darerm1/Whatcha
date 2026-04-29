package com.darerm1.whatcha.presentation.sdui

interface SDUIActionHandler {
    fun onShare(shareText: String)
    fun onAddToFavorites(movieTitle: String, movieId: Long?)
    fun onFavoriteToggled(movieTitle: String, isFavorite: Boolean)
    fun onRatingChanged(rating: Float)
    fun onStatusChanged(newStatus: String, chipText: String)
}
