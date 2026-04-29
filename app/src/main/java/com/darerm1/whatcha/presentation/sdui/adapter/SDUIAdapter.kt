package com.darerm1.whatcha.presentation.sdui.adapter

import android.content.Context
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.darerm1.designsystem.components.ErrorView
import com.darerm1.designsystem.components.MovieCard
import com.darerm1.designsystem.components.PrimaryButton
import com.darerm1.designsystem.components.RatingBar
import com.darerm1.designsystem.components.SearchInput
import com.darerm1.designsystem.components.SecondaryButton
import com.darerm1.designsystem.components.StatusChip
import com.darerm1.whatcha.R
import com.darerm1.whatcha.data.sdui.analytics.AnalyticsTracker
import com.darerm1.whatcha.presentation.sdui.SDUIActionHandler
import com.darerm1.whatcha.presentation.sdui.SDUIItem

class SDUIAdapter(
    private val analyticsTracker: AnalyticsTracker,
    private val actionHandler: SDUIActionHandler? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<SDUIItem>()

    fun submitItems(newItems: List<SDUIItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = items[position].viewType

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val context = parent.context
        return when (viewType) {
            SDUIItem.VIEW_TYPE_MOVIE_CARD -> MovieCardVH(MovieCard(context).apply {
                layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                placeholderRes = R.drawable.poster_placeholder_branded
            })
            SDUIItem.VIEW_TYPE_PRIMARY_BUTTON -> PrimaryButtonVH(PrimaryButton(context).apply {
                layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    val h = context.dpToPx(16)
                    val v = context.dpToPx(8)
                    setMargins(h, v, h, v)
                }
            })
            SDUIItem.VIEW_TYPE_RATING_BAR -> RatingBarVH(RatingBar(context).apply {
                layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    val h = context.dpToPx(16)
                    val v = context.dpToPx(8)
                    setMargins(h, v, h, v)
                }
            })
            SDUIItem.VIEW_TYPE_SEARCH_INPUT -> SearchInputVH(SearchInput(context).apply {
                layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    val m = context.dpToPx(8)
                    setMargins(m, m, m, m)
                }
            })
            SDUIItem.VIEW_TYPE_ERROR_VIEW -> ErrorViewVH(ErrorView(context).apply {
                layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            })
            SDUIItem.VIEW_TYPE_STATUS_CHIP -> StatusChipVH(StatusChip(context).apply {
                layoutParams = RecyclerView.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    val h = context.dpToPx(16)
                    val v = context.dpToPx(8)
                    setMargins(h, v, h, v)
                }
            })
            SDUIItem.VIEW_TYPE_SECONDARY_BUTTON -> SecondaryButtonVH(SecondaryButton(context).apply {
                layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    val h = context.dpToPx(16)
                    val v = context.dpToPx(8)
                    setMargins(h, v, h, v)
                }
            })
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    private fun Context.dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is SDUIItem.MovieCardItem -> (holder as MovieCardVH).bind(item)
            is SDUIItem.PrimaryButtonItem -> (holder as PrimaryButtonVH).bind(item)
            is SDUIItem.RatingBarItem -> (holder as RatingBarVH).bind(item)
            is SDUIItem.SearchInputItem -> (holder as SearchInputVH).bind(item)
            is SDUIItem.ErrorViewItem -> (holder as ErrorViewVH).bind(item)
            is SDUIItem.StatusChipItem -> (holder as StatusChipVH).bind(item)
            is SDUIItem.SecondaryButtonItem -> (holder as SecondaryButtonVH).bind(item)
        }
    }

    // --- ViewHolders ---

    inner class MovieCardVH(private val card: MovieCard) : RecyclerView.ViewHolder(card) {
        fun bind(item: SDUIItem.MovieCardItem) {
            card.title = item.title
            card.yearGenre = item.yearGenre
            card.posterUrl = item.posterUrl
            card.showFavoriteButton = false
        }
    }

    inner class PrimaryButtonVH(private val button: PrimaryButton) : RecyclerView.ViewHolder(button) {
        fun bind(item: SDUIItem.PrimaryButtonItem) {
            button.buttonText = item.buttonText
            button.setOnClickListener {
                item.analytics?.let {
                    analyticsTracker.trackEvent(it.id, it.action)
                }
                when (item.action) {
                    "add_favorite" -> {
                        val movieCard = findMovieCard()
                        actionHandler?.onAddToFavorites(movieCard?.title ?: "", movieCard?.movieId)
                    }
                }
            }
        }
    }

    inner class RatingBarVH(private val ratingBar: RatingBar) : RecyclerView.ViewHolder(ratingBar) {
        fun bind(item: SDUIItem.RatingBarItem) {
            ratingBar.rating = item.rating
            ratingBar.editable = item.editable
            ratingBar.onRatingChangeListener = { newRating ->
                item.analytics?.let {
                    analyticsTracker.trackEvent(it.id, it.action, mapOf("rating" to "$newRating"))
                }
                actionHandler?.onRatingChanged(newRating)
            }
        }
    }

    inner class SearchInputVH(private val searchInput: SearchInput) : RecyclerView.ViewHolder(searchInput) {
        fun bind(item: SDUIItem.SearchInputItem) {
            searchInput.query = item.query
            searchInput.hint = item.hint
            searchInput.onQueryChangeListener = { query ->
                item.analytics?.let {
                    analyticsTracker.trackEvent(it.id, it.action, mapOf("query" to query))
                }
            }
        }
    }

    inner class ErrorViewVH(private val errorView: ErrorView) : RecyclerView.ViewHolder(errorView) {
        fun bind(item: SDUIItem.ErrorViewItem) {
            errorView.errorText = item.errorText
            errorView.buttonText = item.buttonText
            errorView.showButton = item.showButton
            errorView.onRetryClickListener = {
                item.analytics?.let {
                    analyticsTracker.trackEvent(it.id, it.action)
                }
            }
        }
    }

    inner class StatusChipVH(private val chip: StatusChip) : RecyclerView.ViewHolder(chip) {
        fun bind(item: SDUIItem.StatusChipItem) {
            chip.status = item.status
            chip.chipText = item.chipText
            chip.onChipClickListener = {
                item.analytics?.let {
                    analyticsTracker.trackEvent(it.id, it.action)
                }
                showStatusPopup(chip, item)
            }
        }

        private fun showStatusPopup(anchor: StatusChip, item: SDUIItem.StatusChipItem) {
            PopupMenu(anchor.context, anchor).apply {
                menuInflater.inflate(R.menu.menu_status, menu)
                setOnMenuItemClickListener { menuItem ->
                    val (newStatus, chipText) = when (menuItem.itemId) {
                        R.id.status_planned -> "planned" to anchor.context.getString(R.string.status_planned)
                        R.id.status_completed -> "completed" to anchor.context.getString(R.string.status_completed)
                        R.id.status_abandoned -> "abandoned" to anchor.context.getString(R.string.status_abandoned)
                        else -> "not_set" to anchor.context.getString(R.string.status_not_set)
                    }
                    anchor.status = newStatus
                    anchor.chipText = chipText
                    item.analytics?.let {
                        analyticsTracker.trackEvent(it.id, it.action, mapOf("status" to newStatus))
                    }
                    actionHandler?.onStatusChanged(newStatus, chipText)
                    true
                }
                show()
            }
        }
    }

    inner class SecondaryButtonVH(private val button: SecondaryButton) : RecyclerView.ViewHolder(button) {
        fun bind(item: SDUIItem.SecondaryButtonItem) {
            button.buttonText = item.buttonText
            button.setOnClickListener {
                item.analytics?.let {
                    analyticsTracker.trackEvent(it.id, it.action)
                }
                when (item.action) {
                    "share" -> {
                        val text = item.shareText ?: findMovieTitle()
                        actionHandler?.onShare(text)
                    }
                }
            }
        }
    }

    private fun findMovieCard(): SDUIItem.MovieCardItem? {
        return items.filterIsInstance<SDUIItem.MovieCardItem>().firstOrNull()
    }

    private fun findMovieTitle(): String {
        return findMovieCard()?.title ?: ""
    }
}
