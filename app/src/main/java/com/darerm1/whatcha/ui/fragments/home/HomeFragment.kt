package com.darerm1.whatcha.ui.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.darerm1.whatcha.R
import com.darerm1.whatcha.WhatchaApplication
import com.darerm1.whatcha.data.common.NetworkResult
import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.databinding.FragmentHomeBinding
import com.darerm1.whatcha.infrastructure.MovieListService
import com.darerm1.whatcha.ui.NavigationListener
import com.darerm1.whatcha.utils.NetworkErrorHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy { WhatchaApplication.instance.repository }
    private val movieListService = MovieListService.instance
    
    private val adapter by lazy {
        MovieAdapter(
            onFavoriteClick = { movie -> toggleFavorite(movie) },
            onItemClick = { movie -> (activity as? NavigationListener)?.openDetails(movie.id) },
            isFavorite = { movieId -> isFavorite(movieId) }
        )
    }

    private var currentMovies = mutableListOf<MediaItem>()
    private var searchJob: Job? = null
    private var favoriteIds: Set<Long> = emptySet()
    private var isInitialLoadDone = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        refreshFavoriteIds()
        setupRecyclerView()
        setupSearchView()
        setupLoadMoreButton()
        setupRetryButton()
        loadMovies()
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                searchMovies(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupLoadMoreButton() {
        binding.btnLoadMore.setOnClickListener { loadMoreMovies() }
    }
    
    private fun setupRetryButton() {
        binding.retryButton.setOnClickListener { loadMovies() }
    }

    private fun loadMovies() {
        if (isInitialLoadDone && currentMovies.isNotEmpty()) {
            return
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            setLoadingState(true)
            
            when (val result = repository.searchMovies("", limit = 20)) {
                is NetworkResult.Success -> {
                    setLoadingState(false)
                    isInitialLoadDone = true
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
            delay(DEBOUNCE_DELAY)
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
            binding.btnLoadMore.isEnabled = false
            
            when (val result = repository.loadMore()) {
                is NetworkResult.Success -> {
                    val newMovies = result.data.filter { newMovie ->
                        currentMovies.none { it.id == newMovie.id }
                    }
                    currentMovies.addAll(newMovies)
                    adapter.submitList(currentMovies.toList())
                    updateLoadMoreButton()
                }
                is NetworkResult.Error -> {
                    showErrorToast(getString(R.string.error_loading, NetworkErrorHandler.getErrorMessage(requireContext(), result.error)))
                }
            }
            
            binding.btnLoadMore.isEnabled = true
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
        binding.progressBar.isVisible = isLoading
        binding.recyclerView.isVisible = !isLoading
        binding.errorLayout.isVisible = false
        binding.tvEmpty.isVisible = false
    }
    
    private fun showEmptyState() {
        binding.tvEmpty.isVisible = true
        binding.recyclerView.isVisible = false
        binding.progressBar.isVisible = false
        binding.errorLayout.isVisible = false
    }
    
    private fun showErrorState(error: com.darerm1.whatcha.data.common.NetworkError) {
        binding.errorText.text = NetworkErrorHandler.getErrorMessage(requireContext(), error)
        binding.errorLayout.isVisible = true
        binding.recyclerView.isVisible = false
        binding.progressBar.isVisible = false
        binding.tvEmpty.isVisible = false
    }
    
    private fun updateLoadMoreButton() {
        binding.btnLoadMore.isVisible = repository.hasMoreData()
    }
    
    private fun showErrorToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        _binding = null
    }
    
    companion object {
        private const val DEBOUNCE_DELAY = 300L
    }
}
