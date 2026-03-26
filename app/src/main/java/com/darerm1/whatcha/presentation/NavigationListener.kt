package com.darerm1.whatcha.presentation

interface NavigationListener {
    fun openHome()
    fun openFavorites()
    fun openProfile()
    fun openDetails(movieId: Long)
}