package com.darerm1.whatcha.ui

interface NavigationListener {
    fun openHome() // goto F1
    fun openFavorites() // goto F2
    fun openProfile() // goto A2
    fun openDetails(movieId: Long) // goto F3
}