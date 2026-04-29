package com.darerm1.whatcha.presentation.sdui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darerm1.whatcha.data.common.NetworkResult
import com.darerm1.whatcha.data.sdui.network.SDUILoader
import com.darerm1.whatcha.presentation.sdui.SDUIFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SDUIViewModel(
    private val url: String,
    private val loader: SDUILoader,
    private val factory: SDUIFactory
) : ViewModel() {

    private val _state = MutableStateFlow<SDUIState>(SDUIState.Loading)
    val state: StateFlow<SDUIState> = _state.asStateFlow()

    init {
        loadScreen()
    }

    fun loadScreen() {
        viewModelScope.launch {
            _state.value = SDUIState.Loading
            when (val result = loader.loadScreen(url)) {
                is NetworkResult.Success -> {
                    val items = factory.createItems(result.data.components)
                    _state.value = SDUIState.Content(items)
                }
                is NetworkResult.Error -> {
                    _state.value = SDUIState.Error("Не удалось загрузить экран")
                }
            }
        }
    }
}
