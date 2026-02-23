package com.darerm1.whatcha.ui.fragments.details

import android.content.res.ColorStateList
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import coil.load
import com.darerm1.whatcha.R
import com.darerm1.whatcha.data.enums.Status
import com.darerm1.whatcha.data.models.Movie
import com.darerm1.whatcha.databinding.FragmentDetailBinding
import com.darerm1.whatcha.infrastructure.AllMoviesService
import com.darerm1.whatcha.infrastructure.MovieListService
import com.darerm1.whatcha.utils.Result
import com.google.android.material.snackbar.Snackbar

class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null

    private val binding get() = _binding!!

    private val allMoviesService = AllMoviesService.Companion.instance

    private val movieListService = MovieListService.Companion.instance

    private var movieId: Long = -1L

    private lateinit var statusButton: Button

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

        statusButton = view.findViewById(R.id.statusButton)

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.favoriteButton.setOnClickListener { toggleFavorite() }

        binding.saveRatingButton.setOnClickListener {
            updateRating(binding.ratingSlider.value.toInt())
        }

        statusButton.setOnClickListener { showStatusMenu() }

        render()
    }

    private fun render() {
        val movie = allMoviesService.getMovieById(movieId) as? Movie ?: return

        binding.toolbar.title = movie.name
        binding.titleText.text = movie.name
        binding.descriptionText.text = movie.description

        val posterUrl = movie.posterUrl
        if (posterUrl.isNullOrBlank()) {
            binding.posterImage.setImageResource(R.drawable.ic_movie_placeholder)
        } else {
            binding.posterImage.load(posterUrl) {
                placeholder(R.drawable.ic_movie_placeholder)
                error(R.drawable.ic_movie_placeholder)
            }
        }

        val isFavorite = movieListService.getMovies().any { it.id == movie.id }

        binding.favoriteButton.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_filled
            else R.drawable.ic_favorite_border
        )

        val tintAttr = if (isFavorite)
            com.google.android.material.R.attr.colorError
        else
            com.google.android.material.R.attr.colorOnSurface

        binding.favoriteButton.imageTintList =
            ColorStateList.valueOf(resolveThemeColor(tintAttr))

        val rating = movie.personalRating
        if (rating != null) {
            binding.ratingSlider.value = rating.toFloat()
            binding.ratingValue.text =
                getString(R.string.detail_rating_value, rating)
        } else {
            binding.ratingSlider.value = 0f
            binding.ratingValue.text =
                getString(R.string.detail_rating_not_set)
        }

        binding.saveRatingButton.isEnabled = isFavorite

        updateStatusButtonText(movie.status)
    }

    private fun updateStatusButtonText(status: Status) {
        val statusText = getString(R.string.status_label) + " " + when (status) {
            Status.PLANNED -> getString(R.string.status_planned)
            Status.COMPLETED -> getString(R.string.status_completed)
            Status.ABANDONED -> getString(R.string.status_abandoned)
            Status.NOT_SET -> getString(R.string.status_not_set)
        }
        statusButton.text = statusText
    }

    private fun showStatusMenu() {
        PopupMenu(requireContext(), statusButton).apply {
            menuInflater.inflate(R.menu.menu_status, menu)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.status_planned -> {
                        updateStatus(Status.PLANNED)
                        true
                    }
                    R.id.status_completed -> {
                        updateStatus(Status.COMPLETED)
                        true
                    }
                    R.id.status_abandoned -> {
                        updateStatus(Status.ABANDONED)
                        true
                    }
                    R.id.status_not_set -> {
                        updateStatus(Status.NOT_SET)
                        true
                    }
                    else -> false
                }
            }
            show()
        }
    }

    private fun updateStatus(newStatus: Status) {
        val isFav = movieListService.getMovies().any { it.id == movieId }

        if (!isFav) {
            Snackbar.make(
                binding.root,
                getString(R.string.detail_add_to_favorites_first),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        when (newStatus) {
            Status.PLANNED -> movieListService.markAsPlanned(movieId)
            Status.COMPLETED -> movieListService.markAsCompleted(movieId)
            Status.ABANDONED -> movieListService.markAsAbandoned(movieId)
            Status.NOT_SET -> movieListService.markAsNotSet(movieId)
        }

        Snackbar.make(
            binding.root,
            R.string.status_updated,
            Snackbar.LENGTH_SHORT
        ).show()

        render()
    }


    private fun updateRating(value: Int) {
        val isFav = movieListService.getMovies().any { it.id == movieId }

        if (!isFav) {
            Snackbar.make(
                binding.root,
                getString(R.string.detail_add_to_favorites_first),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        val result = movieListService.updateRating(movieId, value)

        val msg = when (result) {
            is Result.Success -> getString(R.string.detail_rating_saved)
            is Result.Error -> result.message
        }

        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
        render()
    }

    private fun toggleFavorite() {
        val movie = allMoviesService.getMovieById(movieId) as? Movie ?: return

        val isFav = movieListService.getMovies().any { it.id == movie.id }

        val result =
            if (isFav)
                movieListService.removeMovieById(movie.id)
            else
                movieListService.addMovie(movie)

        val msg = when (result) {
            is Result.Success ->
                if (isFav)
                    getString(R.string.detail_removed_from_favorites)
                else
                    getString(R.string.detail_added_to_favorites)
            is Result.Error -> result.message
        }

        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
        render()
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

        fun newInstance(movieId: Long): DetailFragment {
            return DetailFragment().apply {
                arguments = bundleOf(ARG_MOVIE_ID to movieId)
            }
        }
    }
}