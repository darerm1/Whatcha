package com.darerm1.whatcha.presentation.fragments.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.darerm1.whatcha.R
import com.darerm1.whatcha.domain.entities.enums.Status
import com.darerm1.whatcha.domain.entities.MediaItem
import com.darerm1.whatcha.presentation.NavigationListener
import com.darerm1.whatcha.presentation.activities.MainActivity
import com.darerm1.whatcha.presentation.fragments.favorites.adapter.FavoritesAdapter
import com.darerm1.whatcha.presentation.fragments.favorites.viewmodel.FavoritesIntent
import com.darerm1.whatcha.presentation.fragments.favorites.viewmodel.FavoritesState
import com.darerm1.whatcha.presentation.fragments.favorites.viewmodel.FavoritesViewModel
import com.darerm1.whatcha.presentation.fragments.favorites.viewmodel.FavoritesViewModelFactory
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private lateinit var viewModel: FavoritesViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private lateinit var buttonSort: Button
    private lateinit var buttonFilter: Button

    private lateinit var adapter: FavoritesAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.favorites_recycler)
        emptyTextView = view.findViewById(R.id.empty_text_view)
        buttonSort = view.findViewById(R.id.btn_sort)
        buttonFilter = view.findViewById(R.id.btn_filter)

        setupRecyclerView()
        setupButtons()

        val useCase = (requireActivity() as MainActivity).useCase
        viewModel = ViewModelProvider(this, FavoritesViewModelFactory(useCase))
            .get(FavoritesViewModel::class.java)

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is FavoritesState.Loading -> showLoading()
                    is FavoritesState.Content -> showContent(
                        movies = state.movies,
                        sort = state.currentSort,
                        sortAscending = state.currentSortAscending,
                        filter = state.currentFilter
                    )
                    is FavoritesState.Error -> showError(state.error)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = FavoritesAdapter(
            movies = emptyList(),
            onItemClick = { movie -> openMovieDetails(movie.id) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        buttonSort.setOnClickListener { showSortMenu() }
        buttonFilter.setOnClickListener { showFilterMenu() }
    }

    private fun showSortMenu() {
        PopupMenu(requireContext(), buttonSort).apply {
            menuInflater.inflate(R.menu.menu_sort, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.sort_by_rating -> {
                        val currentState = viewModel.state.value as? FavoritesState.Content
                        val ascending = if (currentState?.currentSort == "rating") {
                            !currentState.currentSortAscending
                        } else {
                            true
                        }
                        viewModel.processIntent(FavoritesIntent.Sort("rating", ascending))
                        true
                    }
                    R.id.sort_by_year -> {
                        val currentState = viewModel.state.value as? FavoritesState.Content
                        val ascending = if (currentState?.currentSort == "year") {
                            !currentState.currentSortAscending
                        } else {
                            true
                        }
                        viewModel.processIntent(FavoritesIntent.Sort("year", ascending))
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun showFilterMenu() {
        PopupMenu(requireContext(), buttonFilter).apply {
            menuInflater.inflate(R.menu.menu_filter, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.filter_all -> {
                        viewModel.processIntent(FavoritesIntent.Filter(null))
                        true
                    }
                    R.id.filter_completed -> {
                        viewModel.processIntent(FavoritesIntent.Filter(Status.COMPLETED))
                        true
                    }
                    R.id.filter_planned -> {
                        viewModel.processIntent(FavoritesIntent.Filter(Status.PLANNED))
                        true
                    }
                    R.id.filter_abandoned -> {
                        viewModel.processIntent(FavoritesIntent.Filter(Status.ABANDONED))
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun showLoading() {
        recyclerView.visibility = View.GONE
        emptyTextView.visibility = View.GONE
    }

    private fun showContent(movies: List<MediaItem>, sort: String, sortAscending: Boolean, filter: Status?) {
        recyclerView.visibility = View.VISIBLE
        if (movies.isEmpty()) {
            emptyTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyTextView.visibility = View.GONE
            adapter.updateList(movies)
        }

        val directionSymbol = if (sortAscending) "↑" else "↓"
        buttonSort.text = when (sort) {
            "rating" -> getString(R.string.sort_by_rating) + " $directionSymbol"
            "year" -> getString(R.string.sort_by_year) + " $directionSymbol"
            else -> getString(R.string.sort)
        }
        buttonFilter.text = when (filter) {
            Status.COMPLETED -> getString(R.string.filter_completed)
            Status.PLANNED -> getString(R.string.filter_planned)
            Status.ABANDONED -> getString(R.string.filter_abandoned)
            else -> getString(R.string.filter)
        }
    }

    private fun showError(error: com.darerm1.whatcha.domain.common.DomainError) {
        android.widget.Toast.makeText(
            requireContext(),
            com.darerm1.whatcha.presentation.utils.ErrorHandler.getErrorMessage(requireContext(), error),
            android.widget.Toast.LENGTH_SHORT
        ).show()
        recyclerView.visibility = View.GONE
        emptyTextView.visibility = View.VISIBLE
        emptyTextView.text = getString(R.string.error_loading_movies)
    }

    private fun openMovieDetails(movieId: Long) {
        (activity as? NavigationListener)?.openDetails(movieId)
    }

    private fun removeMovie(movie: MediaItem) {
        viewModel.processIntent(FavoritesIntent.RemoveMovie(movie))
    }
}