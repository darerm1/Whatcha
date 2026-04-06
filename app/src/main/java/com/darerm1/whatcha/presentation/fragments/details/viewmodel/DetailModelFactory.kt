package com.darerm1.whatcha.presentation.fragments.details.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.darerm1.whatcha.domain.repositories.AllMoviesRepository
import com.darerm1.whatcha.domain.usecases.ManageMovieListUseCase

class DetailViewModelFactory(
    private val repository: AllMoviesRepository,
    private val useCase: ManageMovieListUseCase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DetailViewModel(repository, useCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}