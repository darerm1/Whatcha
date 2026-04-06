package com.darerm1.whatcha.presentation.fragments.details.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darerm1.whatcha.domain.common.Result
import com.darerm1.whatcha.domain.entities.Movie
import com.darerm1.whatcha.domain.entities.enums.Status
import com.darerm1.whatcha.domain.repositories.AllMoviesRepository
import com.darerm1.whatcha.domain.usecases.ManageMovieListUseCase
import com.darerm1.whatcha.presentation.fragments.details.DetailEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val repository: AllMoviesRepository,
    private val useCase: ManageMovieListUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<DetailState>(DetailState.Loading)
    val state: StateFlow<DetailState> = _state.asStateFlow()

    private val _event = MutableSharedFlow<DetailEvent>()
    val event: SharedFlow<DetailEvent> = _event.asSharedFlow()

    private var currentMovieId: Long = -1L

    fun processIntent(intent: DetailIntent) {
        when (intent) {
            is DetailIntent.Load -> loadMovie(intent.movieId)
            is DetailIntent.ToggleFavorite -> toggleFavorite()
            is DetailIntent.UpdateStatus -> updateStatus(intent.status)
            is DetailIntent.UpdateRating -> updateRating(intent.rating)
        }
    }

    private fun loadMovie(movieId: Long) {
        currentMovieId = movieId
        viewModelScope.launch {
            _state.value = DetailState.Loading
            when (val result = repository.getMovieById(movieId)) {
                is Result.Success -> {
                    val movie = result.data as? Movie ?: return@launch
                    val isFavorite = useCase.getMovies().any { it.id == movie.id }
                    _state.value = DetailState.Content(
                        movie = movie,
                        isFavorite = isFavorite,
                        personalRating = movie.personalRating,
                        status = movie.status
                    )
                }
                is Result.Error -> {
                    _state.value = DetailState.Error(result.error)
                }
            }
        }
    }

    private fun toggleFavorite() {
        val currentState = _state.value
        if (currentState !is DetailState.Content) return

        val movie = currentState.movie
        viewModelScope.launch {
            val isFav = useCase.getMovies().any { it.id == movie.id }
            val result = if (isFav) {
                useCase.removeMovieById(movie.id)
            } else {
                useCase.addMovie(movie)
            }
            if (result is Result.Success) {
                _event.emit(DetailEvent.FavoriteToggled(!isFav))
                loadMovie(currentMovieId)
            } else if (result is Result.Error) {
                _event.emit(DetailEvent.ShowSnackbar(result.error))
            }
        }
    }

    private fun updateStatus(status: Status) {
        val currentState = _state.value
        if (currentState !is DetailState.Content) return

        viewModelScope.launch {
            when (status) {
                Status.PLANNED -> useCase.markAsPlanned(currentMovieId)
                Status.COMPLETED -> useCase.markAsCompleted(currentMovieId)
                Status.ABANDONED -> useCase.markAsAbandoned(currentMovieId)
                Status.NOT_SET -> useCase.markAsNotSet(currentMovieId)
            }
            _event.emit(DetailEvent.StatusUpdated)
            loadMovie(currentMovieId)
        }
    }

    private fun updateRating(rating: Float) {
        val currentState = _state.value
        if (currentState !is DetailState.Content) return

        viewModelScope.launch {
            val result = useCase.updateRating(currentMovieId, rating)
            if (result is Result.Success) {
                _event.emit(DetailEvent.RatingSaved)
                loadMovie(currentMovieId)
            } else if (result is Result.Error) {
                _event.emit(DetailEvent.ShowSnackbar(result.error))
            }
        }
    }
}