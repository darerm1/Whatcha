package com.darerm1.whatcha.ui.fragments.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.darerm1.whatcha.R
import com.darerm1.whatcha.data.enums.Status
import com.darerm1.whatcha.data.interfaces.MediaItem
import com.darerm1.whatcha.data.models.Movie
import com.darerm1.whatcha.infrastructure.MovieListService
import com.darerm1.whatcha.ui.NavigationListener
import com.darerm1.whatcha.utils.Result

class FavoritesFragment : Fragment() {
    private val movieListService = MovieListService.instance

    private lateinit var recyclerView: RecyclerView

    private lateinit var adapter: FavoritesAdapter

    private lateinit var emptyTextView: TextView

    private lateinit var buttonSort: Button

    private lateinit var buttonFilter: Button

    private var currentFilter: Status? = null

    private var currentSort: String = "default"

    private var currentSortAscending: Boolean = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            currentSort = it.getString("currentSort", "default")
            currentSortAscending = it.getBoolean("currentSortAscending", true)
            val filterName = it.getString("currentFilter")
            currentFilter = filterName?.let { name ->
                Status.values().find { it.name == name }
            }
        }

        recyclerView = view.findViewById(R.id.favorites_recycler)
        emptyTextView = view.findViewById(R.id.empty_text_view)
        buttonSort = view.findViewById(R.id.btn_sort)
        buttonFilter = view.findViewById(R.id.btn_filter)

        setupRecyclerView()
        setupButtons()
        updateSortButtonText()
        updateFilterButtonText()

        if (savedInstanceState != null) {
            loadFilteredAndSorted()
        } else {
            loadFavorites()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentSort", currentSort)
        outState.putBoolean("currentSortAscending", currentSortAscending)
        outState.putString("currentFilter", currentFilter?.name)
    }

    private fun setupRecyclerView() {
        adapter = FavoritesAdapter(
            movies = emptyList(),
            onItemClick = { movie ->
                openMovieDetails(movie.id)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        buttonSort.setOnClickListener { showSortMenu() }
        buttonFilter.setOnClickListener { showFilterMenu() }
    }

    private fun updateSortButtonText() {
        val directionSymbol = if (currentSortAscending) "↑" else "↓"

        buttonSort.text = when (currentSort) {
            "rating" -> getString(R.string.sort_by_rating) + " $directionSymbol"
            "year" -> getString(R.string.sort_by_year) + " $directionSymbol"
            else -> getString(R.string.sort)
        }
    }

    private fun updateFilterButtonText() {
        buttonFilter.text = when (currentFilter) {
            Status.COMPLETED -> getString(R.string.filter_completed)
            Status.PLANNED -> getString(R.string.filter_planned)
            Status.ABANDONED -> getString(R.string.filter_abandoned)
            else -> getString(R.string.filter)
        }
    }

    private fun showSortMenu() {
        PopupMenu(requireContext(), buttonSort).apply {
            menuInflater.inflate(R.menu.menu_sort, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.sort_by_rating -> {
                        if (currentSort == "rating") {
                            currentSortAscending = !currentSortAscending
                        } else {
                            currentSort = "rating"
                            currentSortAscending = true
                        }
                        updateSortButtonText()
                        loadFilteredAndSorted()
                        true
                    }
                    R.id.sort_by_year -> {
                        if (currentSort == "year") {
                            currentSortAscending = !currentSortAscending
                        } else {
                            currentSort = "year"
                            currentSortAscending = true
                        }
                        updateSortButtonText()
                        loadFilteredAndSorted()
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
                        currentFilter = null
                        updateFilterButtonText()
                        loadFilteredAndSorted()
                        true
                    }
                    R.id.filter_completed -> {
                        currentFilter = Status.COMPLETED
                        updateFilterButtonText()
                        loadFilteredAndSorted()
                        true
                    }
                    R.id.filter_planned -> {
                        currentFilter = Status.PLANNED
                        updateFilterButtonText()
                        loadFilteredAndSorted()
                        true
                    }
                    R.id.filter_abandoned -> {
                        currentFilter = Status.ABANDONED
                        updateFilterButtonText()
                        loadFilteredAndSorted()
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun loadFilteredAndSorted() {
        val filtered = when (currentFilter) {
            null -> movieListService.getMovies()
            else -> movieListService.getMoviesByStatus(currentFilter!!)
        }

        val sorted = when (currentSort) {
            "year" -> movieListService.getMoviesSortedByYear(currentSortAscending)
            "rating" -> movieListService.getMoviesSortedByRating(currentSortAscending)
            else -> filtered
        }

        if (sorted.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyTextView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyTextView.visibility = View.GONE
            adapter.updateList(sorted)
        }
    }

    private fun loadFavorites() {
        val favorites = movieListService.getMovies()

        if (favorites.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyTextView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyTextView.visibility = View.GONE

            val movies = favorites.filterIsInstance<Movie>()
            adapter.updateList(movies)
        }
    }

    private fun openMovieDetails(movieId: Long) {
        val listener = activity as? NavigationListener
        listener?.openDetails(movieId)
    }

    private fun removeMovie(movie: MediaItem) {
        val result = movieListService.removeMovieById(movie.id)
        if (result is Result.Success) {
            loadFavorites()
        } else if (result is Result.Error) {
            showError(result.exception?.message?: "Ошибка при удалении фильма из списка")
        }
    }

    private fun showError(message: String) {
        android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show()
    }
}
