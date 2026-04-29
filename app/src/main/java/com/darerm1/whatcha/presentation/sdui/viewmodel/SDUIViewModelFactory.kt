package com.darerm1.whatcha.presentation.sdui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.darerm1.whatcha.data.sdui.network.SDUILoader
import com.darerm1.whatcha.presentation.sdui.SDUIFactory

class SDUIViewModelFactory(
    private val url: String,
    private val loader: SDUILoader,
    private val factory: SDUIFactory
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SDUIViewModel::class.java)) {
            return SDUIViewModel(url, loader, factory) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
