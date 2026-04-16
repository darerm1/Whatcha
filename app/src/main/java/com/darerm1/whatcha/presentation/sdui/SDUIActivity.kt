package com.darerm1.whatcha.presentation.sdui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.darerm1.whatcha.R
import com.darerm1.whatcha.WhatchaApplication
import com.darerm1.whatcha.data.sdui.analytics.LogAnalyticsTracker
import com.darerm1.whatcha.data.sdui.network.SDUILoader
import com.darerm1.whatcha.databinding.ActivitySduiBinding
import com.darerm1.whatcha.domain.common.Result
import com.darerm1.whatcha.presentation.sdui.adapter.SDUIAdapter
import com.darerm1.whatcha.presentation.sdui.viewmodel.SDUIState
import com.darerm1.whatcha.presentation.sdui.viewmodel.SDUIViewModel
import com.darerm1.whatcha.presentation.sdui.viewmodel.SDUIViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SDUIActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySduiBinding
    private lateinit var viewModel: SDUIViewModel

    private val repository by lazy { WhatchaApplication.instance.repository }
    private val useCase by lazy { WhatchaApplication.instance.manageMovieListUseCase }

    private val actionHandler = object : SDUIActionHandler {
        override fun onShare(shareText: String) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.sdui_share_chooser)))
        }

        override fun onAddToFavorites(movieTitle: String, movieId: Long?) {
            lifecycleScope.launch {
                if (movieId != null) {
                    when (val result = repository.getMovieById(movieId)) {
                        is Result.Success -> {
                            when (useCase.addMovie(result.data)) {
                                is Result.Success -> showSnackbar(getString(R.string.sdui_added_to_favorites, result.data.name))
                                is Result.Error -> showSnackbar(getString(R.string.error_movie_exists))
                            }
                        }
                        is Result.Error -> showSnackbar(getString(R.string.sdui_add_favorite_error))
                    }
                } else {
                    val searchResult = repository.searchMovies(movieTitle, limit = 1)
                    when (searchResult) {
                        is Result.Success -> {
                            val movie = searchResult.data.firstOrNull()
                            if (movie == null) {
                                showSnackbar(getString(R.string.error_movie_not_found))
                                return@launch
                            }
                            when (useCase.addMovie(movie)) {
                                is Result.Success -> showSnackbar(getString(R.string.sdui_added_to_favorites, movie.name))
                                is Result.Error -> showSnackbar(getString(R.string.error_movie_exists))
                            }
                        }
                        is Result.Error -> showSnackbar(getString(R.string.sdui_add_favorite_error))
                    }
                }
            }
        }

        override fun onFavoriteToggled(movieTitle: String, isFavorite: Boolean) {
            if (isFavorite) {
                onAddToFavorites(movieTitle, null)
            } else {
                val existing = useCase.getMovies().firstOrNull { it.name == movieTitle }
                if (existing != null) {
                    useCase.removeMovieById(existing.id)
                    showSnackbar(getString(R.string.detail_removed_from_favorites))
                }
            }
        }

        override fun onRatingChanged(rating: Float) {
            val movieId = findMovieId()
            if (movieId != null) {
                when (useCase.updateRating(movieId, rating)) {
                    is Result.Success -> showSnackbar(getString(R.string.sdui_rating_saved, rating.toInt()))
                    is Result.Error -> showSnackbar(getString(R.string.sdui_add_favorite_error))
                }
            } else {
                showSnackbar(getString(R.string.sdui_rating_saved, rating.toInt()))
            }
        }

        override fun onStatusChanged(newStatus: String, chipText: String) {
            val movieId = findMovieId()
            if (movieId != null) {
                when (newStatus) {
                    "planned" -> useCase.markAsPlanned(movieId)
                    "completed" -> useCase.markAsCompleted(movieId)
                    "abandoned" -> useCase.markAsAbandoned(movieId)
                    "not_set" -> useCase.markAsNotSet(movieId)
                }
            }
            showSnackbar(getString(R.string.sdui_status_updated, chipText))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySduiBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyNavigationBarStyle()

        val url = intent.getStringExtra(EXTRA_URL)
        if (url.isNullOrBlank()) {
            finish()
            return
        }

        viewModel = ViewModelProvider(
            this,
            SDUIViewModelFactory(url, SDUILoader(), SDUIFactory())
        )[SDUIViewModel::class.java]

        binding.toolbar.setNavigationOnClickListener { finish() }

        val adapter = SDUIAdapter(LogAnalyticsTracker(), actionHandler)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.errorView.onRetryClickListener = { viewModel.loadScreen() }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is SDUIState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.recyclerView.visibility = View.GONE
                        binding.errorView.visibility = View.GONE
                    }
                    is SDUIState.Content -> {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerView.visibility = View.VISIBLE
                        binding.errorView.visibility = View.GONE
                        adapter.submitItems(state.items)
                    }
                    is SDUIState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                        binding.errorView.visibility = View.VISIBLE
                        binding.errorView.errorText = state.message
                    }
                }
            }
        }
    }

    private fun findMovieId(): Long? {
        val state = viewModel.state.value
        if (state is SDUIState.Content) {
            return state.items
                .filterIsInstance<SDUIItem.MovieCardItem>()
                .firstOrNull()
                ?.movieId
        }
        return null
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun applyNavigationBarStyle() {
        val isDarkTheme = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        window.navigationBarColor = if (isDarkTheme) Color.BLACK else Color.WHITE
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = !isDarkTheme
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    companion object {
        const val EXTRA_URL = "sdui_url"
        const val RECOMMENDATION_URL = "https://alfaitmo.ru/server/echo/marssiii/whatcha/sdui/recommendation"

        fun newIntent(context: Context, url: String): Intent {
            return Intent(context, SDUIActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
            }
        }
    }
}
