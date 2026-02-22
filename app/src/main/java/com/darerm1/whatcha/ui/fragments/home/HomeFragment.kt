package com.darerm1.whatcha.ui.fragments.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.darerm1.whatcha.R
import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.infrastructure.AllMoviesService
import com.darerm1.whatcha.infrastructure.MovieListService
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment() {

    private val allMoviesService = AllMoviesService.instance
    private val movieListService = MovieListService.instance

    private val adapter by lazy {
        MovieAdapter(
            onFavoriteClick = { movie -> toggleFavorite(movie) },
            isFavorite = { movieId -> isFavorite(movieId) }
        )
    }

    private val movies = mutableListOf<MediaItem>()
    private var currentPage = 1
    private var currentQuery = ""
    private var hasMore = true
    private var favoriteIds: Set<Long> = emptySet()

    private val debounceHandler = Handler(Looper.getMainLooper())
    private var debounceRunnable: Runnable? = null

    private var recyclerView: androidx.recyclerview.widget.RecyclerView? = null
    private var searchView: SearchView? = null
    private var progressBar: android.widget.ProgressBar? = null
    private var tvEmpty: android.widget.TextView? = null
    private var btnLoadMore: MaterialButton? = null

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
        
        refreshFavoriteIds()
        setupRecyclerView()
        setupSearchView()
        setupLoadMoreButton()
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
                debounceRunnable?.let { debounceHandler.removeCallbacks(it) }
                debounceRunnable = Runnable {
                    currentQuery = newText.orEmpty()
                    currentPage = 1
                    hasMore = true
                    movies.clear()
                    adapter.submitList(emptyList())
                    loadMovies()
                }
                debounceHandler.postDelayed(debounceRunnable!!, 300)
                return true
            }
        })
    }

    private fun setupLoadMoreButton() {
        btnLoadMore?.setOnClickListener {
            loadMovies()
        }
    }

    private fun loadMovies() {
        showLoading(true)
        
        val newMovies = allMoviesService.searchMovies(currentQuery, currentPage, PAGE_SIZE)
        
        if (newMovies.isNotEmpty()) {
            movies.addAll(newMovies)
            adapter.submitList(movies.toList())
            currentPage++
            hasMore = newMovies.size == PAGE_SIZE
        } else {
            hasMore = false
        }

        showLoading(false)
        updateUi()
    }

    private fun toggleFavorite(movie: MediaItem) {
        if (isFavorite(movie.id)) {
            movieListService.removeMovieById(movie.id)
        } else {
            movieListService.addMovie(movie)
        }
        refreshFavoriteIds()
        adapter.notifyItemChanged(movies.indexOf(movie))
    }

    private fun refreshFavoriteIds() {
        favoriteIds = movieListService.getMovies().map { it.id }.toSet()
    }

    private fun isFavorite(movieId: Long): Boolean {
        return favoriteIds.contains(movieId)
    }

    private fun showLoading(show: Boolean) {
        progressBar?.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun updateUi() {
        tvEmpty?.visibility = if (movies.isEmpty()) View.VISIBLE else View.GONE
        btnLoadMore?.visibility = if (hasMore && movies.isNotEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        debounceRunnable?.let { debounceHandler.removeCallbacks(it) }
        recyclerView = null
        searchView = null
        progressBar = null
        tvEmpty = null
        btnLoadMore = null
    }

    companion object {
        private const val PAGE_SIZE = 9
    }
}
