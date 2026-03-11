package com.darerm1.whatcha.ui.fragments.details

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import coil.load
import com.darerm1.whatcha.R
import com.darerm1.whatcha.WhatchaApplication
import com.darerm1.whatcha.data.common.NetworkResult
import com.darerm1.whatcha.data.enums.Status
import com.darerm1.whatcha.data.models.Movie
import com.darerm1.whatcha.data.models.MovieRatings
import com.darerm1.whatcha.databinding.FragmentDetailBinding
import com.darerm1.whatcha.infrastructure.MovieListService
import com.darerm1.whatcha.utils.NetworkErrorHandler
import com.darerm1.whatcha.utils.RatingFormatter
import com.darerm1.whatcha.utils.Result
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    private val repository by lazy { WhatchaApplication.instance.repository }
    private val movieListService = MovieListService.instance

    private var movieId: Long = -1L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        movieId = arguments?.getLong(ARG_MOVIE_ID, -1L) ?: -1L

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.ratingsTitle.text = RATINGS_SECTION_TITLE

        binding.ratingStars.onRatingChange = { rating ->
            updateRatingValue(rating)
            binding.saveRatingButton.isEnabled = isInFavorites() && rating > 0f
        }

        binding.favoriteButton.setOnClickListener { toggleFavorite() }
        binding.saveRatingButton.setOnClickListener { updateRating(binding.ratingStars.rating) }
        binding.statusButton.setOnClickListener { showStatusMenu() }
        binding.retryButton.setOnClickListener { loadMovie() }

        loadMovie()
    }

    private fun loadMovie() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = repository.getMovieById(movieId)) {
                is NetworkResult.Success -> {
                    val movie = result.data as? Movie ?: return@launch
                    displayMovie(movie)
                    showError(false)
                }
                is NetworkResult.Error -> {
                    showError(true, NetworkErrorHandler.getErrorMessage(requireContext(), result.error))
                }
            }
        }
    }

    private fun showError(show: Boolean, message: String = "") {
        binding.errorLayout.isVisible = show
        if (show && message.isNotEmpty()) {
            binding.errorText.text = message
        }
    }

    private fun displayMovie(movie: Movie) {
        binding.toolbar.title = movie.name
        binding.titleText.text = movie.name
        binding.descriptionText.text = movie.description
        binding.metaText.text = getString(
            R.string.detail_meta,
            movie.year,
            movie.genre.russianName,
            movie.duration
        )

        bindRatingsChart(movie.ratings)
        loadPoster(movie.posterUrl)

        val isFavorite = movieListService.getMovies().any { it.id == movie.id }
        updateFavoriteButton(isFavorite)
        updateRatingState(movie.personalRating, isFavorite)
        updateStatusButtonText(movie.status)
    }

    private fun bindRatingsChart(ratings: MovieRatings?) {
        val hasRatings = ratings?.let {
            listOf(
                it.kp,
                it.imdb,
                it.tmdb,
                it.filmCritics,
                it.russianFilmCritics,
                it.await
            ).any { value -> value != null }
        } == true

        binding.ratingsSection.isVisible = hasRatings
        binding.ratingsChart.ratings = ratings
    }

    private fun loadPoster(posterUrl: String?) {
        if (posterUrl.isNullOrBlank()) {
            binding.posterImage.setImageResource(R.drawable.poster_placeholder_branded)
        } else {
            binding.posterImage.load(posterUrl) {
                placeholder(R.drawable.poster_placeholder_branded)
                error(R.drawable.poster_placeholder_branded)
                listener(
                    onSuccess = { _, _ ->
                        Log.d("DetailFragment", "Poster loaded successfully for movie: $movieId")
                    },
                    onError = { _, error ->
                        Log.e(
                            "DetailFragment",
                            "Failed to load poster for movie $movieId: ${error.throwable?.message}"
                        )
                    }
                )
            }
        }
    }

    private fun updateFavoriteButton(isFavorite: Boolean) {
        binding.favoriteButton.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_filled
            else R.drawable.ic_favorite_border
        )

        val tintAttr = if (isFavorite) {
            com.google.android.material.R.attr.colorError
        } else {
            com.google.android.material.R.attr.colorOnSurface
        }

        binding.favoriteButton.imageTintList = ColorStateList.valueOf(resolveThemeColor(tintAttr))
    }

    private fun updateRatingState(rating: Float?, isFavorite: Boolean) {
        binding.ratingStars.rating = rating ?: 0f
        binding.ratingStars.isEnabled = isFavorite
        updateRatingValue(rating ?: 0f)
        binding.saveRatingButton.isEnabled = isFavorite && (rating ?: 0f) > 0f
    }

    private fun updateRatingValue(rating: Float) {
        binding.ratingValue.text = if (rating > 0f) {
            "$PERSONAL_RATING_PREFIX: ${RatingFormatter.formatPersonalRating(rating)}/10"
        } else {
            getString(R.string.detail_rating_not_set)
        }
    }

    private fun updateStatusButtonText(status: Status) {
        val statusText = getString(R.string.status_label) + " " + when (status) {
            Status.PLANNED -> getString(R.string.status_planned)
            Status.COMPLETED -> getString(R.string.status_completed)
            Status.ABANDONED -> getString(R.string.status_abandoned)
            Status.NOT_SET -> getString(R.string.status_not_set)
        }
        binding.statusButton.text = statusText
    }

    private fun showStatusMenu() {
        PopupMenu(requireContext(), binding.statusButton).apply {
            menuInflater.inflate(R.menu.menu_status, menu)
            setOnMenuItemClickListener { item ->
                val status = when (item.itemId) {
                    R.id.status_planned -> Status.PLANNED
                    R.id.status_completed -> Status.COMPLETED
                    R.id.status_abandoned -> Status.ABANDONED
                    R.id.status_not_set -> Status.NOT_SET
                    else -> return@setOnMenuItemClickListener false
                }
                updateStatus(status)
                true
            }
            show()
        }
    }

    private fun updateStatus(newStatus: Status) {
        if (!isInFavorites()) {
            showSnackbar(R.string.detail_add_to_favorites_first)
            return
        }

        when (newStatus) {
            Status.PLANNED -> movieListService.markAsPlanned(movieId)
            Status.COMPLETED -> movieListService.markAsCompleted(movieId)
            Status.ABANDONED -> movieListService.markAsAbandoned(movieId)
            Status.NOT_SET -> movieListService.markAsNotSet(movieId)
        }

        showSnackbar(R.string.status_updated)
        loadMovie()
    }

    private fun updateRating(value: Float) {
        if (!isInFavorites()) {
            showSnackbar(R.string.detail_add_to_favorites_first)
            return
        }

        val result = movieListService.updateRating(movieId, value)

        val message = when (result) {
            is Result.Success -> getString(R.string.detail_rating_saved)
            is Result.Error -> result.message
        }

        showSnackbar(message)
        loadMovie()
    }

    private fun toggleFavorite() {
        viewLifecycleOwner.lifecycleScope.launch {
            when (val result = repository.getMovieById(movieId)) {
                is NetworkResult.Success -> {
                    val movie = result.data as? Movie ?: return@launch
                    val isFav = isInFavorites()

                    val favResult = if (isFav) {
                        movieListService.removeMovieById(movie.id)
                    } else {
                        movieListService.addMovie(movie)
                    }

                    val message = when (favResult) {
                        is Result.Success -> getString(
                            if (isFav) R.string.detail_removed_from_favorites
                            else R.string.detail_added_to_favorites
                        )
                        is Result.Error -> favResult.message
                    }

                    showSnackbar(message)
                    loadMovie()
                }
                is NetworkResult.Error -> {
                    showSnackbar(getString(R.string.error_loading, LOADING_MOVIE_ERROR))
                }
            }
        }
    }

    private fun isInFavorites(): Boolean {
        return movieListService.getMovies().any { it.id == movieId }
    }

    private fun showSnackbar(messageResId: Int) {
        Snackbar.make(binding.root, messageResId, Snackbar.LENGTH_SHORT).show()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun resolveThemeColor(attr: Int): Int {
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MOVIE_ID = "movie_id"
        private const val RATINGS_SECTION_TITLE = "\u0420\u0435\u0439\u0442\u0438\u043d\u0433\u0438 \u043f\u043b\u043e\u0449\u0430\u0434\u043e\u043a"
        private const val PERSONAL_RATING_PREFIX = "\u0412\u0430\u0448\u0430 \u043e\u0446\u0435\u043d\u043a\u0430"
        private const val LOADING_MOVIE_ERROR = "\u041d\u0435 \u0443\u0434\u0430\u043b\u043e\u0441\u044c \u0437\u0430\u0433\u0440\u0443\u0437\u0438\u0442\u044c \u0444\u0438\u043b\u044c\u043c"

        fun newInstance(movieId: Long): DetailFragment {
            return DetailFragment().apply {
                arguments = bundleOf(ARG_MOVIE_ID to movieId)
            }
        }
    }
}