package com.darerm1.whatcha.presentation.sdui.viewmodel

import com.darerm1.whatcha.presentation.sdui.SDUIItem

sealed class SDUIState {
    object Loading : SDUIState()
    data class Content(val items: List<SDUIItem>) : SDUIState()
    data class Error(val message: String) : SDUIState()
}
