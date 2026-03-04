package com.darerm1.whatcha.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.darerm1.whatcha.R
import com.darerm1.whatcha.WhatchaApplication
import com.darerm1.whatcha.data.common.NetworkError
import com.darerm1.whatcha.data.common.NetworkResult
import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.infrastructure.MovieListService
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private val repository by lazy { WhatchaApplication.instance.repository }
    private val movieListService = MovieListService.instance
    
    private val adapter by lazy {
        MovieAdapter(
            onFavoriteClick = { movie -> toggleFavorite(movie) },
            onItemClick = { movie -> (activity as? com.darerm1.whatcha.ui.NavigationListener)?.openDetails(movie.id) },
            isFavorite = { movieId -> isFavorite(movieId) }
        )
    }

    private var currentMovies = mutableListOf<MediaItem>()
    private var searchJob: Job? = null
    private var favoriteIds: Set<Long> = emptySet()

    private var recyclerView: androidx.recyclerview.widget.RecyclerView? = null
    private var searchView: SearchView? = null
    private var progressBar: android.widget.ProgressBar? = null
    private var tvEmpty: android.widget.TextView? = null
    private var btnLoadMore: MaterialButton? = null
    private var errorLayout: LinearLayout? = null
    private var errorText: TextView? = null
    private var retryButton: MaterialButton? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        searchView = view.findViewById(R.id.searchView)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        btnLoadMore = view.findViewById(R.id.btnLoadMore)
        errorLayout = view.findViewById(R.id.errorLayout)
        errorText = view.findViewById(R.id.errorText)
        retryButton = view.findViewById(R.id.retryButton)
        
        refreshFavoriteIds()
        setupRecyclerView()
        setupSearchView()
        setupLoadMoreButton()
        setupRetryButton()
        loadMovies()
    }

    private fun setupRecyclerView() {
        recyclerView?.layoutManager = GridLayoutManager(requireContext(), 3)
        recyclerView?.adapter = adapter
    }

    private fun setupSearchView() {
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchMovies(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupLoadMoreButton() {
        btnLoadMore?.setOnClickListener {
            loadMoreMovies()
        }
    }
    
    private fun setupRetryButton() {
        retryButton?.setOnClickListener {
            loadMovies()
        }
    }

    private fun loadMovies() {
        viewLifecycleOwner.lifecycleScope.launch {
            setLoadingState(true)
            
            when (val result = repository.searchMovies("", limit = 20)) {
                is NetworkResult.Success -> {
                    setLoadingState(false)
                    if (result.data.isEmpty()) {
                        showEmptyState()
                    } else {
                        currentMovies.clear()
                        currentMovies.addAll(result.data)
                        adapter.submitList(currentMovies)
                        updateLoadMoreButton()
                    }
                }
                is NetworkResult.Error -> {
                    setLoadingState(false)
                    showErrorState(result.error)
                }
            }
        }
    }
    
    private fun searchMovies(query: String) {
        searchJob?.cancel()
        searchJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(300) // debounce
            setLoadingState(true)
            
            when (val result = repository.searchMovies(query, limit = 20)) {
                is NetworkResult.Success -> {
                    setLoadingState(false)
                    currentMovies.clear()
                    currentMovies.addAll(result.data)
                    adapter.submitList(currentMovies)
                    updateLoadMoreButton()
                    
                    if (result.data.isEmpty()) {
                        showEmptyState()
                    }
                }
                is NetworkResult.Error -> {
                    setLoadingState(false)
                    showErrorState(result.error)
                }
            }
        }
    }
    
    private fun loadMoreMovies() {
        viewLifecycleOwner.lifecycleScope.launch {
            btnLoadMore?.isEnabled = false
            
            when (val result = repository.loadMore()) {
                is NetworkResult.Success -> {
                    currentMovies.addAll(result.data)
                    adapter.submitList(currentMovies)
                    updateLoadMoreButton()
                }
                is NetworkResult.Error -> {
                    showErrorToast("Ошибка загрузки: ${getErrorText(result.error)}")
                }
            }
            
            btnLoadMore?.isEnabled = true
        }
    }

    private fun toggleFavorite(movie: MediaItem) {
        if (isFavorite(movie.id)) {
            movieListService.removeMovieById(movie.id)
        } else {
            movieListService.addMovie(movie)
        }
        refreshFavoriteIds()
        adapter.notifyItemChanged(currentMovies.indexOf(movie))
    }

    private fun refreshFavoriteIds() {
        favoriteIds = movieListService.getMovies().map { it.id }.toSet()
    }

    private fun isFavorite(movieId: Long): Boolean {
        return favoriteIds.contains(movieId)
    }

    private fun setLoadingState(isLoading: Boolean) {
        progressBar?.isVisible = isLoading
        recyclerView?.isVisible = !isLoading
        errorLayout?.isVisible = false
        tvEmpty?.isVisible = false
    }
    
    private fun showEmptyState() {
        tvEmpty?.isVisible = true
        recyclerView?.isVisible = false
        progressBar?.isVisible = false
        errorLayout?.isVisible = false
    }
    
    private fun showErrorState(error: NetworkError) {
        val message = getErrorText(error)
        
        errorText?.text = message
        errorLayout?.isVisible = true
        recyclerView?.isVisible = false
        progressBar?.isVisible = false
        tvEmpty?.isVisible = false
    }
    
    private fun getErrorText(error: NetworkError): String {
        return when (error) {
            is NetworkError.NoInternet -> "Нет подключения к интернету"
            is NetworkError.Timeout -> "Превышено время ожидания"
            is NetworkError.RateLimitExceeded -> "Дневной лимит запросов исчерпан"
            is NetworkError.Unauthorized -> "Ошибка авторизации API"
            is NetworkError.ServerError -> "Ошибка сервера: ${error.code}"
            is NetworkError.Unknown -> "Неизвестная ошибка: ${error.message}"
        }
    }
    
    private fun updateLoadMoreButton() {
        val hasMore = repository.hasMoreData()
        btnLoadMore?.isVisible = hasMore
    }
    
    private fun showErrorToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        recyclerView = null
        searchView = null
        progressBar = null
        tvEmpty = null
        btnLoadMore = null
        errorLayout = null
        errorText = null
        retryButton = null
    }
}
