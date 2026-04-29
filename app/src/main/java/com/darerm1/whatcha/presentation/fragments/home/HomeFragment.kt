package com.darerm1.whatcha.presentation.fragments.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.darerm1.whatcha.WhatchaApplication
import com.darerm1.whatcha.domain.entities.MediaItem
import com.darerm1.whatcha.databinding.FragmentHomeBinding
import com.darerm1.whatcha.domain.common.DomainError
import com.darerm1.whatcha.presentation.NavigationListener
import com.darerm1.whatcha.presentation.activities.MainActivity
import com.darerm1.whatcha.presentation.fragments.home.adapter.ListItem
import com.darerm1.whatcha.presentation.fragments.home.viewmodel.HomeIntent
import com.darerm1.whatcha.presentation.fragments.home.viewmodel.HomeState
import com.darerm1.whatcha.presentation.fragments.home.viewmodel.HomeViewModel
import com.darerm1.whatcha.presentation.fragments.home.viewmodel.HomeViewModelFactory
import com.darerm1.whatcha.presentation.utils.ErrorHandler
import kotlinx.coroutines.launch
import com.darerm1.whatcha.presentation.fragments.home.adapter.MovieCardAdapter
import com.darerm1.whatcha.presentation.sdui.SDUIActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel

    private var searchJob: Job? = null

    private val adapter by lazy {
        MovieCardAdapter(
            onFavoriteClick = { movie -> viewModel.processIntent(HomeIntent.ToggleFavorite(movie)) },
            onItemClick = { movie -> (activity as? NavigationListener)?.openDetails(movie.id) },
            isFavorite = { movieId -> viewModel.getFavoriteIds().contains(movieId) },
            onLoadMoreClick = {
                Log.d("HomeFragment", "LoadMore clicked")
                viewModel.processIntent(HomeIntent.LoadMore)
            }
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = WhatchaApplication.instance.repository
        val useCase = (requireActivity() as MainActivity).useCase
        viewModel = ViewModelProvider(
            this,
            HomeViewModelFactory(repository, useCase)
        )[HomeViewModel::class.java]

        setupRecyclerView()
        setupSearchInput()
        setupErrorView()

        binding.recommendationCard.setOnClickListener {
            startActivity(SDUIActivity.newIntent(requireContext(), SDUIActivity.RECOMMENDATION_URL))
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is HomeState.Loading -> showLoading()
                    is HomeState.Content -> showContent(state.movies, state.hasMore, state.isSearchMode)
                    is HomeState.Error -> showError(state.error)
                }
            }
        }
    }

    private fun setupSearchInput() {
        binding.searchInput.onQueryChangeListener = { query ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(300)
                viewModel.processIntent(HomeIntent.Search(query))
            }
        }
        binding.searchInput.onClearListener = {
            viewModel.processIntent(HomeIntent.Search(""))
        }
    }

    private fun setupErrorView() {
        binding.errorView.onRetryClickListener = {
            viewModel.processIntent(HomeIntent.Load)
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.recyclerView.isVisible = false
        binding.errorView.isVisible = false
        binding.tvEmpty.isVisible = false
    }

    private fun showContent(movies: List<MediaItem>, hasMore: Boolean, isSearchMode: Boolean) {
        binding.progressBar.isVisible = false
        binding.errorView.isVisible = false
        binding.recyclerView.isVisible = true
        binding.tvEmpty.isVisible = movies.isEmpty()

        val items = movies.map { ListItem.MovieItem(it) }
        adapter.submitMovieList(movies, !isSearchMode && hasMore)
    }

    private fun showError(error: DomainError) {
        binding.progressBar.isVisible = false
        binding.recyclerView.isVisible = false
        binding.tvEmpty.isVisible = false
        binding.errorView.isVisible = true
        binding.errorView.setError(ErrorHandler.getErrorMessage(requireContext(), error), showRetry = true)
    }

    private fun setupRecyclerView() {
        val layoutManager = GridLayoutManager(requireContext(), 3)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.isLoadMorePosition(position)) 3 else 1
            }
        }
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}