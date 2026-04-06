package com.darerm1.whatcha.presentation.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.appcompat.widget.SearchView
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
import com.darerm1.whatcha.presentation.fragments.home.adapter.MovieAdapter
import com.darerm1.whatcha.presentation.fragments.home.viewmodel.HomeIntent
import com.darerm1.whatcha.presentation.fragments.home.viewmodel.HomeState
import com.darerm1.whatcha.presentation.fragments.home.viewmodel.HomeViewModel
import com.darerm1.whatcha.presentation.fragments.home.viewmodel.HomeViewModelFactory
import com.darerm1.whatcha.presentation.utils.ErrorHandler
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HomeViewModel

    private val adapter by lazy {
        MovieAdapter(
            onFavoriteClick = { movie -> viewModel.processIntent(HomeIntent.ToggleFavorite(movie)) },
            onItemClick = { movie -> (activity as? NavigationListener)?.openDetails(movie.id) },
            isFavorite = { movieId -> viewModel.getFavoriteIds().contains(movieId) },
            onLoadMoreClick = {
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
        setupSearchView()
        setupRetryButton()

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

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.recyclerView.isVisible = false
        binding.errorLayout.isVisible = false
        binding.tvEmpty.isVisible = false
    }

    private fun showContent(movies: List<MediaItem>, hasMore: Boolean, isSearchMode: Boolean) {
        binding.progressBar.isVisible = false
        binding.errorLayout.isVisible = false
        binding.recyclerView.isVisible = true
        binding.tvEmpty.isVisible = movies.isEmpty()

        val items = movies.map { ListItem.MovieItem(it) }
        if (!isSearchMode && hasMore) {
            adapter.submitList(items + ListItem.LoadMoreItem)
        } else {
            adapter.submitList(items)
        }
    }

    private fun showError(error: DomainError) {
        binding.progressBar.isVisible = false
        binding.recyclerView.isVisible = false
        binding.tvEmpty.isVisible = false
        binding.errorLayout.isVisible = true
        binding.errorText.text = ErrorHandler.getErrorMessage(requireContext(), error)
    }

    private fun setupRecyclerView() {
        val layoutManager = GridLayoutManager(requireContext(), 3)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.currentList.getOrNull(position)) {
                    is ListItem.LoadMoreItem -> 3
                    else -> 1
                }
            }
        }
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.processIntent(HomeIntent.Search(newText.orEmpty()))
                return true
            }
        })
    }

    private fun setupRetryButton() {
        binding.retryButton.setOnClickListener {
            viewModel.processIntent(HomeIntent.Load)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}