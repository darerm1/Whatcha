package com.darerm1.whatcha.presentation.fragments.home.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darerm1.whatcha.domain.common.Result
import com.darerm1.whatcha.domain.common.SearchConfig
import com.darerm1.whatcha.domain.entities.MediaItem
import com.darerm1.whatcha.domain.repositories.AllMoviesRepository
import com.darerm1.whatcha.domain.usecases.ManageMovieListUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val repository: AllMoviesRepository,
    private val manageMovieListUseCase: ManageMovieListUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Loading)
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var allMovies = mutableListOf<MediaItem>()
    private var currentQuery = ""
    private var isInitialLoadDone = false

    init {
        processIntent(HomeIntent.Load)
    }

    fun processIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.Load -> loadMovies()
            is HomeIntent.Search -> searchMovies(intent.query)
            is HomeIntent.LoadMore -> loadMoreMovies()
            is HomeIntent.ToggleFavorite -> toggleFavorite(intent.movie)
        }
    }

    private fun loadMovies() {
        if (isInitialLoadDone && allMovies.isNotEmpty()) return

        viewModelScope.launch {
            _state.value = HomeState.Loading
            val result = withContext(Dispatchers.IO) {
                repository.searchMovies("", SearchConfig.DEFAULT_PAGE_SIZE)
            }
            when (result) {
                is Result.Success -> {
                    isInitialLoadDone = true
                    allMovies.clear()
                    allMovies.addAll(result.data)
                    _state.value = HomeState.Content(
                        movies = allMovies.toList(),
                        hasMore = repository.hasMoreData(),
                        isSearchMode = false
                    )
                }
                is Result.Error -> {
                    _state.value = HomeState.Error(result.error)
                }
            }
        }
    }

    private fun searchMovies(query: String) {
        currentQuery = query
        val filtered = if (query.isNotEmpty()) {
            allMovies.filter { it.name.contains(query, ignoreCase = true) }
        } else {
            allMovies
        }
        _state.update { state ->
            (state as? HomeState.Content)?.copy(
                movies = filtered,
                isSearchMode = query.isNotEmpty()
            ) ?: HomeState.Content(filtered, repository.hasMoreData(), query.isNotEmpty())
        }
    }

    private fun loadMoreMovies() {
        if (!repository.hasMoreData()) return
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.loadMore()
            }
            when (result) {
                is Result.Success -> {
                    val newMovies = result.data.filter { new ->
                        allMovies.none { it.id == new.id }
                    }
                    allMovies.addAll(newMovies)
                    _state.update { state ->
                        (state as? HomeState.Content)?.copy(
                            movies = allMovies.toList(),
                            hasMore = repository.hasMoreData()
                        ) ?: HomeState.Content(allMovies.toList(), repository.hasMoreData(), false)
                    }
                }
                is Result.Error -> {
                    Log.e("HomeViewModel", "LoadMoreMovies failed: error=${result.error}")
                }
            }
        }
    }

    private fun toggleFavorite(movie: MediaItem) {
        val isFav = manageMovieListUseCase.getMovies().any { it.id == movie.id }
        if (isFav) {
            manageMovieListUseCase.removeMovieById(movie.id)
        } else {
            manageMovieListUseCase.addMovie(movie)
        }
        _state.update { state ->
            (state as? HomeState.Content)?.copy(movies = state.movies.toList())
                ?: state
        }
    }

    fun getFavoriteIds(): Set<Long> =
        manageMovieListUseCase.getMovies().map { it.id }.toSet()
}