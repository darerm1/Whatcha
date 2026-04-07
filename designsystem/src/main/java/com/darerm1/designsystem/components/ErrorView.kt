package com.darerm1.designsystem.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.darerm1.designsystem.R
import com.darerm1.designsystem.databinding.ComponentErrorViewBinding

class ErrorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ComponentErrorViewBinding.inflate(LayoutInflater.from(context), this, true)

    var errorText: String = ""
        set(value) {
            field = value
            binding.tvError.text = value
        }

    var buttonText: String = ""
        set(value) {
            field = value
            binding.btnRetry.text = value
        }

    var showButton: Boolean = true
        set(value) {
            field = value
            binding.btnRetry.visibility = if (value) VISIBLE else GONE
        }

    var onRetryClickListener: (() -> Unit)? = null

    init {
        binding.btnRetry.setOnClickListener {
            onRetryClickListener?.invoke()
        }

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.ErrorView)
            errorText = a.getString(R.styleable.ErrorView_errorText) ?: context.getString(R.string.error_unknown)
            buttonText = a.getString(R.styleable.ErrorView_buttonText) ?: context.getString(R.string.retry)
            showButton = a.getBoolean(R.styleable.ErrorView_showButton, true)
            a.recycle()
        }
    }

    fun setError(message: String, showRetry: Boolean = true) {
        errorText = message
        showButton = showRetry
    }
}
