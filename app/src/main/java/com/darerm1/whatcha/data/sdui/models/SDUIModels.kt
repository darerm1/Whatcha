package com.darerm1.whatcha.data.sdui.models

import com.google.gson.JsonObject

data class SDUIScreen(
    val screenId: String,
    val version: Int,
    val components: List<SDUIComponent>
)

data class SDUIComponent(
    val type: String,
    val id: String,
    val properties: JsonObject,
    val analytics: SDUIAnalytics? = null
)

data class SDUIAnalytics(
    val id: String,
    val action: String
)
