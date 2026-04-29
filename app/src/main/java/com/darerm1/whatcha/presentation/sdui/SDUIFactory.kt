package com.darerm1.whatcha.presentation.sdui

import com.darerm1.whatcha.data.sdui.models.SDUIComponent
import com.google.gson.JsonObject

class SDUIFactory {

    fun createItems(components: List<SDUIComponent>): List<SDUIItem> {
        return components.mapNotNull { component ->
            when (component.type) {
                "movie_card" -> mapMovieCard(component)
                "primary_button" -> mapPrimaryButton(component)
                "rating_bar" -> mapRatingBar(component)
                "search_input" -> mapSearchInput(component)
                "error_view" -> mapErrorView(component)
                "status_chip" -> mapStatusChip(component)
                "secondary_button" -> mapSecondaryButton(component)
                else -> null
            }
        }
    }

    private fun mapMovieCard(c: SDUIComponent) = SDUIItem.MovieCardItem(
        title = c.properties.getStr("title"),
        yearGenre = c.properties.getStr("yearGenre"),
        posterUrl = c.properties.getStrOrNull("posterUrl"),
        isFavorite = c.properties.getBool("isFavorite"),
        movieId = c.properties.getLongOrNull("movieId"),
        analytics = c.analytics
    )

    private fun mapPrimaryButton(c: SDUIComponent) = SDUIItem.PrimaryButtonItem(
        buttonText = c.properties.getStr("buttonText"),
        action = c.properties.getStrOrNull("action"),
        analytics = c.analytics
    )

    private fun mapRatingBar(c: SDUIComponent) = SDUIItem.RatingBarItem(
        rating = c.properties.getFloat("rating"),
        editable = c.properties.getBool("editable", default = true),
        analytics = c.analytics
    )

    private fun mapSearchInput(c: SDUIComponent) = SDUIItem.SearchInputItem(
        query = c.properties.getStr("query"),
        hint = c.properties.getStr("hint"),
        analytics = c.analytics
    )

    private fun mapErrorView(c: SDUIComponent) = SDUIItem.ErrorViewItem(
        errorText = c.properties.getStr("errorText"),
        buttonText = c.properties.getStr("buttonText"),
        showButton = c.properties.getBool("showButton", default = true),
        analytics = c.analytics
    )

    private fun mapStatusChip(c: SDUIComponent) = SDUIItem.StatusChipItem(
        status = c.properties.getStr("status"),
        chipText = c.properties.getStr("chipText"),
        analytics = c.analytics
    )

    private fun mapSecondaryButton(c: SDUIComponent) = SDUIItem.SecondaryButtonItem(
        buttonText = c.properties.getStr("buttonText"),
        action = c.properties.getStrOrNull("action"),
        shareText = c.properties.getStrOrNull("shareText"),
        analytics = c.analytics
    )

    private fun JsonObject.getStr(key: String, default: String = ""): String =
        if (has(key) && !get(key).isJsonNull) get(key).asString else default

    private fun JsonObject.getStrOrNull(key: String): String? =
        if (has(key) && !get(key).isJsonNull) get(key).asString else null

    private fun JsonObject.getBool(key: String, default: Boolean = false): Boolean =
        if (has(key) && !get(key).isJsonNull) get(key).asBoolean else default

    private fun JsonObject.getFloat(key: String, default: Float = 0f): Float =
        if (has(key) && !get(key).isJsonNull) get(key).asFloat else default

    private fun JsonObject.getLongOrNull(key: String): Long? =
        if (has(key) && !get(key).isJsonNull) get(key).asLong else null
}
