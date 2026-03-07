package com.darerm1.whatcha.ui.fragments.home

import android.os.Bundle
import android.util.Log
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
import com.darerm1.whatcha.repositories.RemoteMoviesRepository
import com.darerm1.whatcha.ui.NavigationListener
import com.darerm1.whatcha.utils.NetworkErrorHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            
            val result = withContext(Dispatchers.IO) {
                repository.searchMovies("", RemoteMoviesRepository.PAGE_SIZE)
            }
            
            when (result) {
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
            
            val result = withContext(Dispatchers.IO) {
                repository.searchMovies(query, RemoteMoviesRepository.PAGE_SIZE)
            }
            
            when (result) {
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
        Log.d("HomeFragment_DEBUG", "=== loadMoreMovies START ===")
        Log.d("HomeFragment_DEBUG", "currentMovies size before: ${currentMovies.size}")
        
        viewLifecycleOwner.lifecycleScope.launch {
            binding.btnLoadMore.isEnabled = false
            
            val result = withContext(Dispatchers.IO) {
                repository.loadMore()
            }
            
            Log.d("HomeFragment_DEBUG", "result type: ${result::class.simpleName}")
            
            when (result) {
                is NetworkResult.Success -> {
                    Log.d("HomeFragment_DEBUG", "Success: received ${result.data.size} movies from API")
                    
                    result.data.take(5).forEachIndexed { index, movie ->
                        Log.d("HomeFragment_DEBUG", "API movie[$index]: id=${movie.id}, name=${movie.name}")
                    }
                    
                    currentMovies.take(5).forEachIndexed { index, movie ->
                        Log.d("HomeFragment_DEBUG", "Current movie[$index]: id=${movie.id}, name=${movie.name}")
                    }
                    
                    val newMovies = result.data.filter { newMovie ->
                        currentMovies.none { it.id == newMovie.id }
                    }
                    
                    Log.d("HomeFragment_DEBUG", "After filtering: newMovies size=${newMovies.size}")
                    
                    if (newMovies.isEmpty()) {
                        Log.w("HomeFragment_DEBUG", "NO NEW MOVIES - all were duplicates!")
                    } else {
                        Log.d("HomeFragment_DEBUG", "Adding ${newMovies.size} new movies")
                        newMovies.forEach { movie ->
                            Log.d("HomeFragment_DEBUG", "New movie: id=${movie.id}, name=${movie.name}")
                        }
                    }
                    
                    currentMovies.addAll(newMovies)
                    Log.d("HomeFragment_DEBUG", "currentMovies size after: ${currentMovies.size}")
                    
                    adapter.submitList(currentMovies.toList()) {
                        Log.d("HomeFragment_DEBUG", "submitList callback executed")
                    }
                    
                    updateLoadMoreButton()
                }
                is NetworkResult.Error -> {
                    Log.e("HomeFragment_DEBUG", "Error: ${result.error}")
                    showErrorToast(getString(R.string.error_loading, NetworkErrorHandler.getErrorMessage(requireContext(), result.error)))
                }
            }
            
            binding.btnLoadMore.isEnabled = true
            Log.d("HomeFragment_DEBUG", "=== loadMoreMovies END ===")
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
        val hasMore = repository.hasMoreData()
        Log.d("HomeFragment_DEBUG", "updateLoadMoreButton: hasMoreData=$hasMore")
        binding.btnLoadMore.isVisible = hasMore
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
