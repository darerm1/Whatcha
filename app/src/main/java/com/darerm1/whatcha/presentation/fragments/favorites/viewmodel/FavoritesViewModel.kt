package com.darerm1.whatcha.presentation.fragments.favorites.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darerm1.whatcha.domain.common.Result
import com.darerm1.whatcha.domain.entities.MediaItem
import com.darerm1.whatcha.domain.entities.enums.Status
import com.darerm1.whatcha.domain.usecases.ManageMovieListUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val manageMovieListUseCase: ManageMovieListUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<FavoritesState>(FavoritesState.Loading)
    val state: StateFlow<FavoritesState> = _state.asStateFlow()

    private var currentSort: String = "default"
    private var currentSortAscending: Boolean = true
    private var currentFilter: Status? = null
    private var allMovies: List<MediaItem> = emptyList()

    init {
        processIntent(FavoritesIntent.Load)
    }

    fun processIntent(intent: FavoritesIntent) {
        when (intent) {
            is FavoritesIntent.Load -> loadMovies()
            is FavoritesIntent.Sort -> updateSort(intent.sortBy, intent.ascending)
            is FavoritesIntent.Filter -> updateFilter(intent.status)
            is FavoritesIntent.RemoveMovie -> removeMovie(intent.movie)
        }
    }

    private fun loadMovies() {
        viewModelScope.launch {
            _state.value = FavoritesState.Loading
            allMovies = manageMovieListUseCase.getMovies()
            applyFiltersAndSort()
        }
    }

    private fun updateSort(sortBy: String, ascending: Boolean) {
        currentSort = sortBy
        currentSortAscending = ascending
        applyFiltersAndSort()
    }

    private fun updateFilter(status: Status?) {
        currentFilter = status
        applyFiltersAndSort()
    }

    private fun removeMovie(movie: MediaItem) {
        viewModelScope.launch {
            val result = manageMovieListUseCase.removeMovieById(movie.id)
            if (result is Result.Success) {
                loadMovies()
            } else if (result is Result.Error) {
                _state.update { state ->
                    (state as? FavoritesState.Content)?.copy(movies = state.movies) ?: state
                }
                Log.e("FavoritesViewModel", "RemoveMovie failed: error=${result.error}")
            }
        }
    }

    private fun applyFiltersAndSort() {
        // Фильтрация
        val filtered = if (currentFilter != null) {
            manageMovieListUseCase.getMoviesByStatus(currentFilter!!)
        } else {
            allMovies
        }

        // Сортировка
        val sorted = when (currentSort) {
            "year" -> manageMovieListUseCase.getMoviesSortedByYear(currentSortAscending)
            "rating" -> manageMovieListUseCase.getMoviesSortedByRating(currentSortAscending)
            else -> filtered
        }

        _state.value = FavoritesState.Content(
            movies = sorted,
            currentSort = currentSort,
            currentSortAscending = currentSortAscending,
            currentFilter = currentFilter
        )
    }
}