package com.darerm1.whatcha.presentation.fragments.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.darerm1.whatcha.domain.repositories.AllMoviesRepository
import com.darerm1.whatcha.domain.usecases.ManageMovieListUseCase

class HomeViewModelFactory(
    private val repository: AllMoviesRepository,
    private val useCase: ManageMovieListUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}