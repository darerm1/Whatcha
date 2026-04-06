package com.darerm1.whatcha.presentation.fragments.favorites.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.darerm1.whatcha.domain.usecases.ManageMovieListUseCase
import com.darerm1.whatcha.presentation.fragments.favorites.viewmodel.FavoritesViewModel

class FavoritesViewModelFactory(
    private val useCase: ManageMovieListUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoritesViewModel(useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}